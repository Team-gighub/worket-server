package gighub.worketserver.service;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.ContractFile;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.global.exception.TransactionErrorCode;
import gighub.worketserver.global.exception.TransactionException;
import gighub.worketserver.repository.ContractFileRepository;
import gighub.worketserver.repository.ContractRepository;
import gighub.worketserver.repository.TransactionRepository;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 거래(Transaction) 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
  public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final ContractRepository contractRepository;
  private final ContractFileRepository contractFileRepository;

  /**
   * 거래 전체 조회 (월별)
   */
  public TransactionListResponse getTransactions(Authentication authentication, Integer year, Integer month) {
    Long userId = Long.parseLong(authentication.getName());

    // 기본값 설정
    if (year == null) year = LocalDate.now().getYear();
    if (month == null) month = LocalDate.now().getMonthValue();

    log.info("Getting transactions for user {} - {}/{}", userId, year, month);

    // 1. 사용자 정보 조회
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));


    // 2. 해당 월의 거래 목록 조회
    List<Transaction> transactions = transactionRepository
      .findByFreelancerIdAndYearMonth(userId, year, month);

    // 3. 거래 목록을 DTO로 변환
    List<TransactionSummaryDto> contractList = transactions.stream()
      .map(this::convertToTransactionSummary)
      .collect(Collectors.toList());

    // 4. 해당 월의 총 거래 금액 계산
    BigDecimal totalAmount = transactions.stream()
      .map(Transaction::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 5. 상태별 건수 계산 (해당 월의 거래만)
    List<StatusCountDto> statusCounts = calculateStatusCountsForMonth(transactions);

    // 6. 응답 생성
    return TransactionListResponse.builder()
      .freelancerName(user.getName())
      .totalAmount(totalAmount.toString())
      .statusCounts(statusCounts)
      .contractList(contractList)
      .build();
  }

  /**
   * 거래 정보 미리보기
   * 특정 거래 ID에 해당하는 미리보기 정보를 조회합니다.
   * * @param transactionId 조회할 거래 ID
   * @return 거래 미리보기 응답 객체
   * @throws RuntimeException 해당 transactionId에 대한 정보가 없을 경우 (400 처리)
   */
  @Transactional(readOnly = true)
  public TransactionPreviewResponse getTransactionPreview(Long transactionId) {
    log.info("Getting transaction preview for transaction {}", transactionId);

    Transaction transaction = transactionRepository.findByIdWithContractAndUsers(transactionId)
      .orElseThrow(() -> {
        log.warn("Transaction preview not found for ID: {}", transactionId);
        throw new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다.");
      });


    Contract contract = transaction.getContract();
    User freelancer = contract.getFreelancer();

    return TransactionPreviewResponse.builder()
      .title(contract.getTitle())
      .freelancerName(freelancer.getName())
      //client가 매핑되지 않은 경우를 고려해 contract에 있는 clientName반환
      .clientName(contract.getClientName())
      .build();
  }

  /**
   * 거래 접근권한 판단
   * * @param transactionId 조회할 거래 ID
   */
  @Transactional
  public TransactionPermissionResponse checkPermission(Authentication authentication, Long transactionId) {
    // 1. 토큰에서 사용자 ID 추출 및 사용자 정보 조회
    Long userId = Long.parseLong(authentication.getName());
    log.info("Checking permission for user {} on transaction {}", userId, transactionId);

    // 사용자 정보를 DB에서 조회합니다.
    User currentUser = userRepository.findById(userId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 2. transactionId로 거래 조회 (EntityGraph로 N+1 문제 방지)
    Transaction transaction = transactionRepository.findByIdWithContractAndUsers(transactionId)
      .orElseThrow(() -> {
        log.warn("Transaction not found. transactionId={}", transactionId);
        return new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다.");
      });

    Contract contract = transaction.getContract();
    if (contract == null) {
      log.error("Transaction {} has no associated contract.", transactionId);
      throw new RestApiException(CommonErrorCode.NOT_FOUND, "거래에 연결된 계약이 없습니다.");
    }

    boolean permission = false;
    String role = null; // 초기 역할은 null
    log.info("Access granted for freelancer: {}", contract.getFreelancer());

    // 3. 접근 권한 판단 로직
    if (contract.getClient() != null) {
      // Case A: 클라이언트가 등록된 사용자(clientId가 존재)인 경우 -> ID 기반 접근 권한 판단
      Long clientId = contract.getClient().getId();

      if (userId.equals(clientId)) {
        permission = true;
        role = Role.CLIENT.name();
      } else if (userId.equals(contract.getFreelancer().getId())) {
        permission = true;
        role = Role.FREELANCER.name();
      }

    } else {
      // Case B: 클라이언트가 등록되지 않은 사용자(clientId가 null)인 경우
      log.info("client name: {}, client phone{}", contract.getClientName(),contract.getClientPhone());
      // 3-1. 현재 사용자가 이름/전화번호로 확인되는 미등록 클라이언트인지 확인
      if (currentUser.getName().equals(contract.getClientName())
        && currentUser.getPhone().equals(contract.getClientPhone())) {
        log.info("Access granted by Name/Phone for client: {},{}", contract.getClientName(),contract.getClientPhone());


        contract.updateClient(currentUser);
        contractRepository.save(contract);

        permission = true;
        role = Role.CLIENT.name();
      }

      // 3-2. 현재 사용자가 Freelancer인지 확인 (ID 기반)
      else if (userId.equals(contract.getFreelancer().getId())) {
        permission = true;
        role = Role.FREELANCER.name();

        log.info("Access granted for freelancer: {}", userId);
      }
    }

    // 4. 최종 권한 확인
    if (!permission) {
      log.warn("Access denied for user {} on transaction {}", userId, transactionId);
      throw new TransactionException(TransactionErrorCode.TRANSACTION_ACCESS_DENIED);
    }

    return new TransactionPermissionResponse(role,permission);
  }

  /**
   * 거래 상세 조회
   */
  public TransactionDetailResponse getTransactionDetail(Authentication authentication, Long transactionId) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Getting transaction detail for transaction {} by user {}", transactionId, userId);

    // 1단계: Transaction 존재 여부 확인
    if (!transactionRepository.existsByTransactionId(transactionId)) {
      log.warn("Transaction not found: {}", transactionId);
      throw new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다.");
    }

    // 2단계: 권한 확인 (프리랜서 또는 의뢰인인지)
    if (!transactionRepository.hasPermission(transactionId, userId)) {
      log.warn("User {} has no permission for transaction {}", userId, transactionId);
      throw new RestApiException(CommonErrorCode.FORBIDDEN_ACCESS, "거래에 대한 접근 권한이 없습니다.");
    }

    // 3단계: 실제 데이터 조회 (LEFT JOIN FETCH)
    Transaction transaction = transactionRepository.findByIdWithContractAndUsers(transactionId)
      .orElseThrow(() ->  new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

    Contract contract = transaction.getContract();

    ContractFile contractFile = contractFileRepository.findByContractId(contract.getId());


    // 4단계: DTO 변환 시 null 안전 처리
    ClientInfoDto clientInfoDto = null;
    FreelancerInfoDto freelancerInfoDto = null;
    if (contract != null) {
      clientInfoDto = ClientInfoDto.builder()
        .name(contract.getClientName())
        .phone(contract.getClientPhone())
        .build();
      if (contract.getFreelancer() != null) {
        freelancerInfoDto = FreelancerInfoDto.builder()
          .name(contract.getFreelancer().getName())
          .phone(contract.getFreelancer().getPhone())
          .account(transaction.getFreelancerAccount())
          .bank(transaction.getFreelancerBank())
          .build();
      }
    }

    return TransactionDetailResponse.builder()
      .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
      .signedAt(transaction.getSignedAt() != null ? transaction.getSignedAt().format(DATETIME_FORMATTER) : null)
      .depositHoldAt(transaction.getDepositHoldAt() != null ? transaction.getDepositHoldAt().format(DATETIME_FORMATTER) : null)
      .paymentConfirmedAt(transaction.getPaymentConfirmedAt() != null ? transaction.getPaymentConfirmedAt().format(DATETIME_FORMATTER) : null)
      .settledAt(transaction.getSettledAt() != null ? transaction.getSettledAt().format(DATETIME_FORMATTER) : null)
      .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().format(DATETIME_FORMATTER) : null)
      .contractId(contract != null ? contract.getId() : null)
      .settledAmount(transaction.getSettlementAmount())
      .contractFileUrl(contractFile != null ?  contractFile.getFileUrl()+ "contract.pdf" : null)
      .escrowId(transaction.getEscrowConfimId() !=null ?  transaction.getEscrowConfimId() : null)
      .contractInfo(contract != null ? ContractInfoDto.builder()
        .title(contract.getTitle())
        .amount(contract.getAmount())
        .startDate(contract.getStartDate() != null ? contract.getStartDate().format(DATE_FORMATTER) : null)
        .endDate(contract.getEndDate() != null ? contract.getEndDate().format(DATE_FORMATTER) : null)
        .build() : null)
      .clientInfo(clientInfoDto)
      .freelancerInfo(freelancerInfoDto)
      .build();
  }

  /**
   * Transaction을 TransactionSummaryDto로 변환
   */
  private TransactionSummaryDto convertToTransactionSummary(Transaction transaction) {
    Contract contract = transaction.getContract();
    return TransactionSummaryDto.builder()
      .transactionId(transaction.getId())
      .title(contract != null ? contract.getTitle() : null)
      .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
      .amount(transaction.getAmount())
      .startDate(contract != null && contract.getStartDate() != null ? contract.getStartDate().format(DATE_FORMATTER) : null)
      .endDate(contract != null && contract.getEndDate() != null ? contract.getEndDate().format(DATE_FORMATTER) : null)
      .build();
  }

  /**
   * 상태별 거래 건수 계산 (해당 월의 거래만)
   */
  private List<StatusCountDto> calculateStatusCountsForMonth(List<Transaction> transactions) {
    Map<TransactionStatus, Long> statusCountMap = transactions.stream()
      .collect(Collectors.groupingBy(Transaction::getStatus, Collectors.counting()));

    return Arrays.stream(TransactionStatus.values())
      .map(status -> new StatusCountDto(
        status.name(),
        statusCountMap.getOrDefault(status, 0L).intValue()
      ))
      .collect(Collectors.toList());
  }
}
