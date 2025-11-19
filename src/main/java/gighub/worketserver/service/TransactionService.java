package gighub.worketserver.service;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
   * 특정 거래 ID에 해당하는 미리보기 정보를 조회합니다.
   * * @param transactionId 조회할 거래 ID
   * @return 거래 미리보기 응답 객체
   * @throws RuntimeException 해당 transactionId에 대한 정보가 없을 경우 (400 처리)
   */
  @Transactional(readOnly = true)
  public TransactionPreviewResponse getTransactionPreview(Long transactionId) {
    log.info("Getting transaction preview for transaction {}", transactionId);

    Transaction transaction = transactionRepository.findById(transactionId)
      .orElseThrow(() -> {
        log.warn("Transaction preview not found for ID: {}", transactionId);
        //TODO: 전역 에러 핸들러로 수정 필요
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionId를 찾을 수 없습니다.");
      });


    Contract contract = transaction.getContract();
    User freelancer = contract.getFreelancer();
    User client = contract.getClient();

    return TransactionPreviewResponse.builder()
      .title(contract.getTitle())
      .freelancerName(freelancer.getName())
      .clientName(client.getName())
      .build();
  }

  /**
   * 거래 접근권한 판단
   * * @param transactionId 조회할 거래 ID
   */
  @Transactional(readOnly = true)
  public TransactionPermissionResponse checkPermission(Authentication authentication, Long transactionId) {
    // 1. 토큰에서 사용자 ID 추출
    Long userId = Long.parseLong(authentication.getName());
    log.info("Checking permission for user {} on transaction {}", userId, transactionId);

    // 2. 거래 정보 및 관련 계약, 사용자 정보를 한 번에 조회
    Transaction transaction = transactionRepository.findById(transactionId)
      .orElseThrow(() -> {
        log.warn("Permission check failed: Transaction {} not found.", transactionId);
        //TODO: 전역 에러 핸들러로 수정 필요
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, transactionId + "를 찾을 수 없습니다.");
      });

    Contract contract = transaction.getContract();

    // 3. 데이터 무결성 체크
    if (contract == null || contract.getClient() == null || contract.getFreelancer() == null) {
      log.error("Data integrity failure: Contract or User data is missing for transaction {}", transactionId);
      //TODO: 전역 에러 핸들러로 수정 필요
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "거래 데이터가 불완전합니다 (계약 정보 또는 사용자 연결 누락).");
    }

    Long clientId = contract.getClient().getId();
    Long freelancerId = contract.getFreelancer().getId();

    // 4. 권한 및 역할 확인
    if (userId.equals(clientId)) {
      // 사용자가 클라이언트인 경우
      return TransactionPermissionResponse.builder()
        .userRole(Role.CLIENT.name())
        .permission(true)
        .build();
    } else if (userId.equals(freelancerId)) {
      // 사용자가 프리랜서인 경우
      return TransactionPermissionResponse.builder()
        .userRole(Role.FREELANCER.name())
        .permission(true)
        .build();
    } else {
      // 해당 거래와 관계가 없는 사용자 (권한 없음)
      log.warn("Permission denied: User {} is not a participant in transaction {}", userId, transactionId);
      return TransactionPermissionResponse.builder()
        .userRole("GUEST") // GUEST 또는 NONE으로 역할 표시
        .permission(false)
        .build();
    }
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
