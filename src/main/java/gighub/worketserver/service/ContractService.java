package gighub.worketserver.service;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.ContractType;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.repository.ContractRepository;
import gighub.worketserver.repository.TransactionRepository;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

  /**
   * 계약서 추출 (OCR + LLM)
   */
  public ContractExtractResponse extractContract(Authentication authentication, MultipartFile file) {
    log.info("Extracting contract from file: {}", file.getOriginalFilename());

    // Mock: OCR + LLM 처리 결과
    return ContractExtractResponse.builder()
      .contractInfo(ContractInfoDto.builder()
        .title("웹 개발 프로젝트")
        .amount(BigDecimal.valueOf(5000000))
        .startDate(LocalDate.now().toString())
        .endDate(LocalDate.now().plusMonths(3).toString())
        .build())
      .clientInfo(ClientInfoDto.builder()
        .name("김의뢰")
        .phone("010-1234-5678")
        .build())
      .freelancerInfo(FreelancerInfoDto.builder()
        .name("이프리")
        .phone("010-9876-5432")
        .account("110-123-456789")
        .bank("신한은행")
        .build())
      .build();
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
    
    // Mock: Contract 생성
    Contract contract = Contract.builder()
      .type(request.getType())
      .title(request.getContractInfo().getTitle())
      .amount(request.getContractInfo().getAmount())
      .startDate(LocalDate.parse(request.getContractInfo().getStartDate()))
      .endDate(LocalDate.parse(request.getContractInfo().getEndDate()))
      .freelancerName(request.getFreelancerInfo().getName())
      .freelancerPhone(request.getFreelancerInfo().getPhone())
      .freelancerAccount(request.getFreelancerInfo().getAccount())
      .freelancerBank(request.getFreelancerInfo().getBank())
      .clientName(request.getClientInfo().getName())
      .clientPhone(request.getClientInfo().getPhone())
      .build();

    Contract savedContract = contractRepository.save(contract);

    // Mock: Transaction 생성
    Transaction transaction = Transaction.builder()
      .contract(savedContract)
      .freelancer(freelancer)  // User 객체로 설정
      .status(TransactionStatus.CREATED)
      .createdAt(LocalDateTime.now())
      .build();

    Transaction savedTransaction = transactionRepository.save(transaction);

    return ContractCreateResponse.builder()
      .transactionId(savedTransaction.getId())
      .build();
  }

  /**
   * 서명 등록
   */
  @Transactional
  public void registerSignature(Authentication authentication, Long contractId, SignatureRequest request) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Registering signature for contract {} by user {}", contractId, userId);

    // Mock: 서명 저장 로직
    Contract contract = contractRepository.findById(contractId)
      .orElseThrow(() -> new RuntimeException("Contract not found"));

    // TODO: 서명 URL 저장, Transaction 상태 업데이트
    log.info("Signature URL: {}", request.getSignatureUrl());
  }
}
