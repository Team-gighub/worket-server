package gighub.worketserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.ContractFile;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.ContractType;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import gighub.worketserver.repository.ContractFileRepository;
import gighub.worketserver.repository.ContractRepository;
import gighub.worketserver.repository.TransactionRepository;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
  private final S3Service s3Service;
  private final ContractFileRepository contractFileRepository;


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

      // 4. 계약서 pdf 파일을 세션에 저장
      result.put("pdfFile", file.getBytes());

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
  public ContractCreateResponse createContract(Authentication authentication, ContractCreateRequest request) throws NoSuchAlgorithmException, IOException {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Creating contract for user {}: {}", userId, request.getContractInfo().getTitle());

    // User 조회
    User freelancer = userRepository.findById(userId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "유저 정보가 존재하지 않습니다."));

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

    ContractType type = request.getType();
    TransactionStatus status;

    if (type == ContractType.UPLOAD) {
      //업로드는 거래 타입이 SINGED
      status = TransactionStatus.SIGNED;
    } else if (type == ContractType.CREATED) {
      //생성은 거래 타입이 CREATED
      status = TransactionStatus.CREATED;
    } else {
      throw new RestApiException(CommonErrorCode.BAD_REQUEST, "계약서형태가 올바르지 않습니다.");
    }
    Transaction transaction = Transaction.builder()
      .contract(savedContract) //생성된 계약서 주입
      .amount(request.getContractInfo().getAmount())
      .freelancerBank(request.getFreelancerInfo().getBank())
      .freelancerAccount(request.getFreelancerInfo().getAccount())
      .status(status)
      .build();

    // Transaction 저장
    Transaction savedTransaction = transactionRepository.save(transaction);

    // S3 업로드
    byte[] toUploadFile = request.getPdfFile();

    // 1. 메타데이터 준비
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("contractInfo", request.getContractInfo());
    metadata.put("clientInfo", request.getClientInfo());
    metadata.put("freelancerInfo", request.getFreelancerInfo());
    byte[] metadataJson = objectMapper.writeValueAsBytes(metadata);

    // 2. PDF + 메타데이터 합치기
    ByteArrayOutputStream combinedStream = new ByteArrayOutputStream();
    combinedStream.write(toUploadFile);
    combinedStream.write(metadataJson);
    byte[] combinedBytes = combinedStream.toByteArray();

    // 3. 해시 생성 (PDF + 메타데이터 기반)
    String hashValue = generateHash(combinedBytes);

    // 4. S3 업로드
    // 4-1. PDF 업로드
    String uploadedContractFile = s3Service.uploadContractFile(
      toUploadFile,
      savedContract.getId() + "/contract.pdf",
      "application/pdf"
    );

    // 4-2. 해시 업로드
    s3Service.uploadContractFile(
      hashValue.getBytes(StandardCharsets.UTF_8),
      savedContract.getId() + "/hash.txt",
      "text/plain"
    );

    // 4-3. 메타데이터 업로드
    s3Service.uploadContractFile(
      metadataJson,
      savedContract.getId() + "/metadata.json",
      "application/json"
    );

    // contract_file 테이블 저장
    ContractFile contractFile = ContractFile.builder()
      .contract(savedContract)
      .fileUrl(s3Service.extractContractPath(uploadedContractFile)) // pdf, json, txt가 담긴 폴더 url로 전달
      .fileHash(hashValue)
      .build();

    contractFileRepository.save(contractFile);

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
    PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
    Role role = principal.getUser().getRole();
    log.info("Registering signature for contract {} by user {}", contractId, userId);

    // 서명 저장 로직
    Contract contract = contractRepository.findById(contractId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "계약 정보가 존재하지 않습니다."));

    Transaction transaction = transactionRepository.findByContract(contract)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "계약 정보가 존재하지 않습니다."));

    //프리랜서의 경우 저장
    if (role.equals(Role.FREELANCER)) {
      contract.updateFreelancerSignUrl(request.getSignatureUrl());
    }//클라이언트의 경우 저장, 상태 바꾸고
    else if (role.equals(Role.CLIENT)) {
      contract.updateClientSignUrl(request.getSignatureUrl());
      TransactionStatus status = TransactionStatus.SIGNED;
      transaction.updateStatus(status);
    }


    log.info("Signature URL: {}", request.getSignatureUrl());
  }

  /**
   * PDF 파일 HASH 값으로 변환
   *
   * @param pdfBytes
   * @return
   */
  private String generateHash(byte[] pdfBytes) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(pdfBytes);
    StringBuilder sb = new StringBuilder();
    for (byte b : hashBytes) sb.append(String.format("%02x", b));
    return sb.toString();
  }
}
