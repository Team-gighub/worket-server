package gighub.worketserver.service;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.ErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 거래(Transaction) 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

  private final TransactionRepository transactionRepository;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /**
   * 거래 전체 조회 (월별)
   */
  public TransactionListResponse getTransactions(Authentication authentication, Integer year, Integer month) {
    Long userId = Long.parseLong(authentication.getName());

    // 기본값 설정
    if (year == null) year = LocalDate.now().getYear();
    if (month == null) month = LocalDate.now().getMonthValue();

    log.info("Getting transactions for user {} - {}/{}", userId, year, month);

    // Mock: 거래 목록 생성
    List<TransactionSummaryDto> contractList = new ArrayList<>();
    contractList.add(TransactionSummaryDto.builder()
      .transactionId(1L)
      .title("웹 개발 프로젝트")
      .status(TransactionStatus.SIGNED.name())
      .amount(BigDecimal.valueOf(5000000))
      .startDate(LocalDate.of(year, month, 1).toString())
      .endDate(LocalDate.of(year, month, 15).toString())
      .build());

    contractList.add(TransactionSummaryDto.builder()
      .transactionId(2L)
      .title("앱 디자인 프로젝트")
      .status(TransactionStatus.DEPOSIT_HOLD.name())
      .amount(BigDecimal.valueOf(3000000))
      .startDate(LocalDate.of(year, month, 5).toString())
      .endDate(LocalDate.of(year, month, 20).toString())
      .build());

    // Mock: 상태별 건수
    List<StatusCountDto> statusCounts = new ArrayList<>();
    statusCounts.add(new StatusCountDto("CREATED", 2));
    statusCounts.add(new StatusCountDto("SIGNED", 5));
    statusCounts.add(new StatusCountDto("DEPOSIT_HOLD", 3));
    statusCounts.add(new StatusCountDto("PAYMENT_CONFIRMED", 1));
    statusCounts.add(new StatusCountDto("SETTLED", 10));

    return TransactionListResponse.builder()
      .freelancerName("이프리랜서")
      .totalAmount("8000000")
      .statusCounts(statusCounts)
      .contractList(contractList)
      .build();
  }

  /**
   * 거래 정보 미리보기
   */
  public TransactionPreviewResponse getTransactionPreview(Long transactionId) {
    log.info("Getting transaction preview for transaction {}", transactionId);

    // Mock: 거래 미리보기
    return TransactionPreviewResponse.builder()
      .freelancerName("이프리랜서")
      .clientName("김의뢰인")
      .title("웹 개발 프로젝트")
      .build();
  }

  /**
   * 거래 접근권한 판단
   */
  public TransactionPermissionResponse checkPermission(Authentication authentication, Long transactionId) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Checking permission for user {} on transaction {}", userId, transactionId);

    // Mock: 권한 체크
    // TODO: 실제 거래 의뢰인 매핑 상태 확인 및 권한 부여 로직

    return TransactionPermissionResponse.builder()
      .userRole(Role.CLIENT.name())
      .permission(true)
      .build();
  }

  /**
   * 거래 상세 조회
   */
  public TransactionDetailResponse getTransactionDetail(
    Authentication authentication,
    Long transactionId) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Getting transaction detail for transaction {} by user {}", transactionId, userId);

    // 1단계: Transaction 존재 여부 확인
    if (!transactionRepository.existsByTransactionId(transactionId)) {
      log.warn("Transaction not found: {}", transactionId);
      throw new RestApiException(ErrorCode.NOT_FOUND);
    }

    // 2단계: 권한 확인 (로그인한 프리랜서의 거래인지)
    if (!transactionRepository.hasPermission(transactionId, userId)) {
      log.warn("User {} has no permission for transaction {}", userId, transactionId);
      throw new RestApiException(ErrorCode.FORBIDDEN_ACCESS);
    }

    // 3단계: 실제 데이터 조회 (JOIN FETCH)
    Transaction transaction = transactionRepository.findByIdWithDetails(transactionId)
      .orElseThrow(() -> new RestApiException(ErrorCode.NOT_FOUND)); // 이론상 발생 안 함

    Contract contract = transaction.getContract();

    // 4단계: DTO 변환 및 반환
    return TransactionDetailResponse.builder()
      .status(transaction.getStatus().name())
      .signedAt(transaction.getSignedAt() != null
        ? transaction.getSignedAt().format(DATETIME_FORMATTER)
        : null)
      .depositHoldAt(transaction.getDepositHoldAt() != null
        ? transaction.getDepositHoldAt().format(DATETIME_FORMATTER)
        : null)
      .paymentConfirmedAt(transaction.getPaymentConfirmedAt() != null
        ? transaction.getPaymentConfirmedAt().format(DATETIME_FORMATTER)
        : null)
      .settledAt(transaction.getSettledAt() != null
        ? transaction.getSettledAt().format(DATETIME_FORMATTER)
        : null)
      .createdAt(transaction.getCreatedAt().format(DATETIME_FORMATTER))
      .contractId(contract.getId())
      .settledAmount(transaction.getSettlementAmount())
      .contractFileUrl("https://s3.amazonaws.com/bucket/contract-" + contract.getId() + ".pdf") // TODO: 실제 S3 URL
      .contractInfo(ContractInfoDto.builder()
        .title(contract.getTitle())
        .amount(contract.getAmount())
        .startDate(contract.getStartDate() != null
          ? contract.getStartDate().format(DATE_FORMATTER)
          : null)
        .endDate(contract.getEndDate() != null
          ? contract.getEndDate().format(DATE_FORMATTER)
          : null)
        .build())
      .clientInfo(ClientInfoDto.builder()
        .name(contract.getClient().getName())
        .phone(contract.getClient().getPhone())
        .build())
      .freelancerInfo(FreelancerInfoDto.builder()
        .name(contract.getFreelancer().getName())
        .phone(contract.getFreelancer().getPhone())
        .account(transaction.getFreelancerAccount())
        .bank(transaction.getFreelancerBank())
        .build())
      .build();
  }
}
