package gighub.worketserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.repository.ContractRepository;
import gighub.worketserver.repository.TransactionRepository;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 계약서 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractService {

  private final ContractRepository contractRepository;
  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final OcrService ocrService;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;


  /**
   * 계약서 추출 (OCR + LLM)
   */
  public ApiResponse<?> extractContract(MultipartFile file, String message) {
    try {
      // 1. OCR 실행
      String ocrJson = ocrService.processOcr(file, message);

      // 2. LLM 실행 (OCR을 통해 받아온 값을 넘겨줌)
      String llmJson = geminiService.getRawGeminiResponse(ocrJson);

      // 3. JSON → Map 변환
      Map<String, Object> result = objectMapper.readValue(
        llmJson,
        new TypeReference<Map<String, Object>>() {
        }
      );

      return ApiResponse.ok(result);

    } catch (Exception e) {
      // TODO : custom error 도입
      throw new RuntimeException(e);
    }
  }

  /**
   * 계약서 등록
   */
  @Transactional
  public ContractCreateResponse createContract(Authentication authentication, ContractCreateRequest request) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Creating contract for user {}: {}", userId, request.getContractInfo().getTitle());

    // User 조회
    User freelancer = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));


    // Contract 생성
    Contract contract = Contract.builder()
      .type(request.getType())
      .title(request.getContractInfo().getTitle())
      .amount(request.getContractInfo().getAmount())
      .startDate(LocalDate.parse(request.getContractInfo().getStartDate()))
      .endDate(LocalDate.parse(request.getContractInfo().getEndDate()))
      .freelancer(freelancer)  // 프리랜서 객체
      .clientName(request.getClientInfo().getName()) //클라이언트 이름
      .clientPhone(request.getClientInfo().getPhone())//클라이언트 전화번호
      .build();

    //계약서 생성
    Contract savedContract = contractRepository.save(contract);

    // Transaction 생성
    Transaction transaction = Transaction.builder()
      .contract(savedContract) //생성된 계약서 주입
      .amount(request.getContractInfo().getAmount())
      .freelancerBank(request.getFreelancerInfo().getBank())
      .freelancerAccount(request.getFreelancerInfo().getAccount())
      .status(TransactionStatus.CREATED)
      .createdAt(LocalDateTime.now())
      .build();

    // Transaction 저장
    Transaction savedTransaction = transactionRepository.save(transaction);
    //System.out.println(savedTransaction.getId());

    return ContractCreateResponse.builder()
      .transactionId(savedTransaction.getId()) //거래 ID
      .contractId(savedContract.getId()) // 계약 ID
      .build();
  }

  /**
   * 서명 등록
   */
  @Transactional
  public void registerSignature(Authentication authentication, Long contractId, SignatureRequest request) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Registering signature for contract {} by user {}", contractId, userId);

    // 서명 저장 로직
    Contract contract = contractRepository.findById(contractId)
      .orElseThrow(() -> new RuntimeException("Contract not found"));

    // TODO: 서명 URL 저장, Transaction 상태 업데이트
    contract.updateFreelancerSignUrl(request.getSignatureUrl());
    contractRepository.save(contract);
    Transaction transaction = transactionRepository.findByContract(contract);
    TransactionStatus status = TransactionStatus.SIGNED;

    transaction.updateStatus(status);
    transactionRepository.save(transaction);
    log.info("Signature URL: {}", request.getSignatureUrl());
  }
}
