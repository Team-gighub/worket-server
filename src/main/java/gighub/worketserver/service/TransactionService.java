package gighub.worketserver.service;

import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Mock: 거래 상세 정보
    return TransactionDetailResponse.builder()
      .status(TransactionStatus.SIGNED.name())
      .signedAt(LocalDateTime.now().minusDays(5).toString())
      .depositHoldAt(null)
      .paymentConfirmedAt(null)
      .settledAt(null)
      .createdAt(LocalDateTime.now().minusDays(10).toString())
      .contractId(1L)
      .settledAmount(BigDecimal.valueOf(5000000))
      .contractFileUrl("https://s3.amazonaws.com/bucket/contract-1.pdf")
      .contractInfo(ContractInfoDto.builder()
        .title("웹 개발 프로젝트")
        .amount(BigDecimal.valueOf(5000000))
        .startDate(LocalDate.now().minusDays(5).toString())
        .endDate(LocalDate.now().plusDays(85).toString())
        .build())
      .clientInfo(ClientInfoDto.builder()
        .name("김의뢰인")
        .phone("010-1234-5678")
        .build())
      .freelancerInfo(FreelancerInfoDto.builder()
        .name("이프리랜서")
        .phone("010-9876-5432")
        .account("110-123-456789")
        .bank("신한은행")
        .build())
      .build();
  }
}
