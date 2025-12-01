package gighub.worketserver.service;

import gighub.worketserver.client.CoreApiClient;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.CoreApprovalRequest;
import gighub.worketserver.dto.PaymentApprovalResponse;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final CoreApiClient coreApiClient;
  private final TransactionRepository transactionRepository;


  @Transactional(readOnly = true)
  public PaymentApprovalResponse approval(Long transactionId ,String escrowId, String confirmToken) {
    // 1) 계정계 서버로 요청 생성
    CoreApprovalRequest request = new CoreApprovalRequest(
      escrowId,
      confirmToken
    );

    // 2) 계정계 서버 호출
    PaymentApprovalResponse coreResp = coreApiClient.requestPaymentApproval(request);

    if (coreResp == null) {
      throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR,"core 접근 중 에러 발생");
    }

    log.info("▶ 계정계 서버 승인 응답 도착 → escrowId={}, status={}",
      coreResp.getEscrowId(), coreResp.getHoldStatus());

    // 3) transaction 테이블 업데이트
    Transaction tx = transactionRepository.findById(transactionId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "Transaction을 찾을 수 없습니다"));

    tx.setEscrowConfimId(coreResp.getEscrowId());
    transactionRepository.save(tx);

    //status 업데이트("DEPOSIT_HOLD")
    TransactionStatus status = TransactionStatus.DEPOSIT_HOLD;
    tx.updateStatus(status);

    // 4) 컨트롤러에 그대로 반환할 응답 조립
    return PaymentApprovalResponse.builder()
      .escrowId(coreResp.getEscrowId())
      .holdStatus(coreResp.getHoldStatus())
      .holdAmount(coreResp.getHoldAmount())
      .platformFee(coreResp.getPlatformFee())
      .holdStartDatetime(coreResp.getHoldStartDatetime())
      .build();
  }

}
