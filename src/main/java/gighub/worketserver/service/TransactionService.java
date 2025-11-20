package gighub.worketserver.service;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
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
  private final UserRepository userRepository;

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

    Transaction transaction = transactionRepository.findByIdWithContractAndUsers(transactionId)
      .orElseThrow(() -> {
        log.warn("Transaction preview not found for ID: {}", transactionId);
        //TODO: 전역 에러 핸들러로 수정 필요 [거래 없을 경우]
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "거래를 찾을 수 없습니다.");
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
  @Transactional(readOnly = true)
  public TransactionPermissionResponse checkPermission(Authentication authentication, Long transactionId) {
    // 1. 토큰에서 사용자 ID 추출 및 사용자 정보 조회
    Long userId = Long.parseLong(authentication.getName());
    log.info("Checking permission for user {} on transaction {}", userId, transactionId);

    // (가정) 사용자 정보를 DB에서 조회합니다.
    User currentUser = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인된 사용자 정보를 찾을 수 없습니다."));

    // 2. transactionId로 거래 조회 (EntityGraph로 N+1 문제 방지)
    Transaction transaction = transactionRepository.findByIdWithContractAndUsers(transactionId)
      .orElseThrow(() -> {
        log.warn("Transaction not found. transactionId={}", transactionId);
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "거래를 찾을 수 없습니다.");
      });

    Contract contract = transaction.getContract();
    if (contract == null) {
      log.error("Transaction {} has no associated contract.", transactionId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "거래에 연결된 계약이 없습니다.");
    }

    boolean permission = false;
    String role = null; // 초기 역할은 null

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

      // 3-1. 현재 사용자가 이름/전화번호로 확인되는 미등록 클라이언트인지 확인
      if (currentUser.getName().equals(contract.getClientName())
        && currentUser.getPhone().equals(contract.getClientPhone())) {
        permission = true;
        role = Role.CLIENT.name();

        log.info("Access granted by Name/Phone for client: {}", contract.getClientName());

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
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "거래에 대한 접근 권한이 없습니다.");
    }

    return new TransactionPermissionResponse(role,permission);
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
