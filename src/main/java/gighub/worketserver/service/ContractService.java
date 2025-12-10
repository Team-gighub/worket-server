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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import gighub.worketserver.dto.UploadResultDTO;

import static gighub.worketserver.service.TransactionService.DATE_FORMATTER;

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
  private final PdfGenerationService pdfGenerationService;


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

    Long userId = extractUserId(authentication);// User 조회
    User freelancer = getFreelancer(userId);
    log.info("Creating contract for user {}: {}", userId, request.getContractInfo().getTitle());

    Contract contract = createAndSaveContract(request, freelancer); // Contract 생성
    Transaction transaction = createAndSaveTransaction(request, contract); // Transaction 생성
    UploadResultDTO uploadResult = null;
    if (request.getType() == ContractType.UPLOAD) {
      uploadResult = uploadContractFilesToS3(request, contract); // S3 업로드
      saveContractFileRecord(contract, uploadResult.getUploadedContractFileUrl(), uploadResult.getHash()); // contract file table에 저장
    }

    return buildCreateResponse(transaction, contract);
  }

  /**
   * 서명 등록
   */
  @Transactional
  public void registerSignature(Authentication authentication, Long contractId, SignatureRequest request) throws IOException, NoSuchAlgorithmException {
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
    }
    /* 클라이언트의 경우 저장
    * 1. db에 서명 저장
    * 2. 계약서 pdf만들기
    * 3. 만들어진 pdf s3에 올리기
    * 4. db에 pdfUrl 저장
    * 5. status 업데이트("SIGNED")
    * */
    else if (role.equals(Role.CLIENT)) {
      log.info("user role: {}",role);
      // 1. db에 서명 저장
      contract.updateClientSignUrl(request.getSignatureUrl());
      contractRepository.save(contract);

      //2. 계약서 pdf만들기
      byte[] pdfBytes = pdfGenerationService.generateContractPdf(contract);

      log.info("pdfBytes done! ");

      //2-1. ContractCreateRequest 생성
      ContractCreateRequest contractCreateRequest = buildContractCreateRequest(contract, transaction, pdfBytes);
      log.info("contractCreateRequest : {}", contractCreateRequest);
      //3. 만들어진 계약 s3에 올리기
      UploadResultDTO uploadResult = null;
      uploadResult = uploadContractFilesToS3(contractCreateRequest, contract); // S3 업로드



      //4. db에 contractFile 저장
      saveContractFileRecord(contract, uploadResult.getUploadedContractFileUrl(), uploadResult.getHash());

      //5. status 업데이트("SIGNED")
      TransactionStatus status = TransactionStatus.SIGNED;
      transaction.updateStatus(status);
    }


    log.info("Signature URL: {}", request.getSignatureUrl());
  }


  /**
   * 인증/회원 조회
   *
   * @param authentication
   * @return
   */
  private Long extractUserId(Authentication authentication) {
    return Long.parseLong(authentication.getName());
  }

  private User getFreelancer(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "유저 정보가 존재하지 않습니다."));
  }

  /**
   * 계약 생성
   *
   * @param request
   * @param freelancer
   * @return
   */
  private Contract createAndSaveContract(ContractCreateRequest request, User freelancer) {
    Contract contract = Contract.builder()
      .type(request.getType())
      .title(request.getContractInfo().getTitle())
      .amount(request.getContractInfo().getAmount())
      .startDate(LocalDate.parse(request.getContractInfo().getStartDate()))
      .endDate(LocalDate.parse(request.getContractInfo().getEndDate()))
      .freelancer(freelancer)
      .clientName(request.getClientInfo().getName())
      .clientPhone(request.getClientInfo().getPhone())
      .build();

    return contractRepository.save(contract);
  }

  /**
   * 계약 상태 결정
   *
   * @param type
   * @return
   */
  private TransactionStatus determineStatus(ContractType type) {
    if (type == ContractType.UPLOAD) return TransactionStatus.SIGNED;
    if (type == ContractType.CREATED) return TransactionStatus.CREATED;
    throw new RestApiException(CommonErrorCode.BAD_REQUEST, "계약서형태가 올바르지 않습니다.");
  }

  /**
   * 거래 생성
   *
   * @param request
   * @param contract
   * @return
   */
  private Transaction createAndSaveTransaction(ContractCreateRequest request, Contract contract) {
    Transaction transaction = Transaction.builder()
      .contract(contract)
      .amount(request.getContractInfo().getAmount())
      .freelancerBank(request.getFreelancerInfo().getBank())
      .freelancerAccount(request.getFreelancerInfo().getAccount())
      .status(determineStatus(request.getType()))
      .build();

    return transactionRepository.save(transaction);
  }

  /**
   * 메타데이터 빌더
   *
   * @param request
   * @return
   */
  private Map<String, Object> buildMetadata(ContractCreateRequest request) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("contractInfo", request.getContractInfo());
    metadata.put("clientInfo", request.getClientInfo());
    metadata.put("freelancerInfo", request.getFreelancerInfo());
    return metadata;
  }


  /**
   * 계약서 파일과 메타데이터 결합
   *
   * @param pdfBytes
   * @param metadataBytes
   * @return
   * @throws IOException
   */
  private byte[] combinePdfAndMetadata(byte[] pdfBytes, byte[] metadataBytes) throws IOException {
    ByteArrayOutputStream combinedStream = new ByteArrayOutputStream();
    combinedStream.write(pdfBytes);
    combinedStream.write(metadataBytes);
    return combinedStream.toByteArray();
  }

  /**
   * 해시 값 생성
   *
   * @param bytes
   * @return
   * @throws NoSuchAlgorithmException
   */
  private String generateHash(byte[] bytes) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(bytes);
    StringBuilder sb = new StringBuilder();
    for (byte b : hashBytes) sb.append(String.format("%02x", b));
    return sb.toString();
  }

  /**
   * S3 업로드
   *
   * @param request
   * @param contract
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  private UploadResultDTO uploadContractFilesToS3(ContractCreateRequest request, Contract contract)
    throws IOException, NoSuchAlgorithmException {

    byte[] pdfBytes = request.getPdfFile();

    Map<String, Object> metadata = buildMetadata(request);
    byte[] metadataJson = objectMapper.writeValueAsBytes(metadata);

    byte[] combinedBytes = combinePdfAndMetadata(pdfBytes, metadataJson);
    String hash = generateHash(combinedBytes);

    String uploadedContractFile = s3Service.uploadContractFile(pdfBytes, contract.getId() + "/contract.pdf", "application/pdf");
    s3Service.uploadContractFile(hash.getBytes(StandardCharsets.UTF_8), contract.getId() + "/hash.txt", "text/plain");
    try {
      s3Service.uploadContractFile(
        metadataJson,
        contract.getId() + "/metadata.json",
        "application/json"
      );
    } catch (Exception e) {
      log.warn("metadata.json 업로드 실패 - 무시하고 계속 진행합니다. contractId={}, error={}",
        contract.getId(), e.getMessage());
    }

    return new UploadResultDTO(uploadedContractFile, hash);
  }

  /**
   * 계약서 파일 저장
   *
   * @param contract
   * @param uploadedContractFileUrl
   * @param hash
   */
  private void saveContractFileRecord(Contract contract, String uploadedContractFileUrl, String hash) {
    String folderPath = s3Service.extractContractPath(uploadedContractFileUrl);
    ContractFile record = ContractFile.builder()
      .contract(contract)
      .fileUrl(folderPath)
      .fileHash(hash)
      .build();

    contractFileRepository.save(record);
  }

  /**
   * 계약서 생성 응답
   *
   * @param transaction
   * @param contract
   * @return
   */
  private ContractCreateResponse buildCreateResponse(Transaction transaction, Contract contract) {
    return ContractCreateResponse.builder()
      .transactionId(transaction.getId())
      .contractId(contract.getId())
      .build();
  }

   /**
    *
    * @param contract   계약 엔티티
    * @param transaction 거래 엔티티 (프리랜서 계좌 및 은행 정보 포함)
    * @param pdfBytes   생성된 계약서 PDF 파일의 byte 배열
    * @return ContractCreateRequest 계약서 생성 요청 DTO
    */
  private ContractCreateRequest buildContractCreateRequest(Contract contract, Transaction transaction, byte[] pdfBytes) {

    ClientInfoDto clientInfoDto = null;
    FreelancerInfoDto freelancerInfoDto = null;

    if (contract != null) {
      if (contract.getClient() != null) {
        clientInfoDto = ClientInfoDto.builder()
          .name(contract.getClient().getName())
          .phone(contract.getClient().getPhone())
          .build();
      }
      if (contract.getFreelancer() != null) {
        freelancerInfoDto = FreelancerInfoDto.builder()
          .name(contract.getFreelancer().getName())
          .phone(contract.getFreelancer().getPhone())
          .account(transaction.getFreelancerAccount())
          .bank(transaction.getFreelancerBank())
          .build();
      }
    }

    return ContractCreateRequest.builder()
      .type(contract.getType())
      .contractInfo(
        ContractInfoDto.builder()
          .title(contract.getTitle())
          .amount(contract.getAmount())
          .startDate(contract.getStartDate() != null ? contract.getStartDate().format(DATE_FORMATTER) : null)
          .endDate(contract.getEndDate() != null ? contract.getEndDate().format(DATE_FORMATTER) : null)
          .build()
      )
      .clientInfo(clientInfoDto)
      .freelancerInfo(freelancerInfoDto)
      .pdfFile(pdfBytes)
      .build();
  }


}
