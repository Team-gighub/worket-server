package gighub.worketserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import gighub.worketserver.client.CoreApiClient;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.TransactionStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.PgErrorCode;
import gighub.worketserver.global.exception.PgException;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.global.response.ApiResponse;
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
  private final ObjectMapper objectMapper;


  @Transactional
  public PaymentApprovalResponse approval(Long transactionId ,String escrowId, String confirmToken) throws JsonProcessingException {
    try {
      // 1) 계정계 서버로 요청 생성
      CoreApprovalRequest request = new CoreApprovalRequest(
        escrowId,
        confirmToken
      );

      // 2) 계정계 서버 호출
      CoreApprovalResponse coreResp = coreApiClient.requestPaymentApproval(request).getData();

      if (coreResp == null) {
        throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, "core 접근 중 에러 발생");
      }

      log.info("▶ 계정계 서버 승인 응답 도착 → escrowId={}, status={}",
        coreResp.getEscrowId(), coreResp.getHoldStatus());

      // 3) transaction 테이블 업데이트
      Transaction tx = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "Transaction을 찾을 수 없습니다"));

      tx.setEscrowConfirmId(coreResp.getEscrowId());
      tx.setClientBank(coreResp.getPayerBankCode());
      tx.setClientAccount(coreResp.getPayerAccount());

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

      } catch (FeignException.FeignClientException ex) {
        // 계정계에서 내려준 에러 JSON 그대로 파싱
        String content = ex.contentUTF8();
        log.error("[Feign Error] 채널계 요청 중 오류 발생. Body: {}", content);
        if (content == null || content.isEmpty()) {
          log.warn("[Feign Mapping] Empty content from Feign exception. Default to INTERNAL_SERVER_ERROR.");
          throw new PgException(PgErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode root = objectMapper.readTree(content);
        JsonNode errorNode = root.path("error");
        String accountErrorCode = errorNode.path("code").asText();

        PgErrorCode mappedErrorCode = PgErrorCode.fromPgErrorCode(accountErrorCode);
        log.info("pg error code: {}", mappedErrorCode);

        // 커스텀 예외로 throw
        throw new PgException(mappedErrorCode);

      }
  }

  @Transactional
  public CoreConfirmResponse confirm(Long transactionId ,String escrowId, String merchantId) throws JsonProcessingException {
    try {
      // 1) 계정계 서버로 요청 생성
      CoreConfirmRequest request = new CoreConfirmRequest(
        merchantId,
        escrowId
      );

      // 2) 계정계 서버 호출
      ApiResponse<CoreConfirmResponse> res = coreApiClient.requestPaymentConfirm(request);
      CoreConfirmResponse coreResp = res.getData();

      if (coreResp == null) {
        throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, "core 접근 중 에러 발생");
      }

      log.info("▶ 계정계 서버 승인 응답 도착 → paymentId={}",
        coreResp.getPaymentId());

      // 3) transaction 테이블 업데이트
      Transaction tx = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "Transaction을 찾을 수 없습니다"));

      tx.setSettlementId(coreResp.getPaymentId());

      //status 업데이트("PAYMENT_CONFIRMED")
      TransactionStatus status = TransactionStatus.PAYMENT_CONFIRMED;
      tx.updateStatus(status);

      // 4) 컨트롤러에 그대로 반환할 응답 조립
      return CoreConfirmResponse.builder()
        .paymentId(coreResp.getPaymentId())
        .build();

    } catch (FeignException.FeignClientException ex) {
      // 계정계에서 내려준 에러 JSON 그대로 파싱
      String content = ex.contentUTF8();
      log.error("[Feign Error] 채널계 요청 중 오류 발생. Body: {}", content);
      if (content == null || content.isEmpty()) {
        log.warn("[Feign Mapping] Empty content from Feign exception. Default to INTERNAL_SERVER_ERROR.");
        throw new PgException(PgErrorCode.INTERNAL_SERVER_ERROR);
      }

      JsonNode root = objectMapper.readTree(content);
      JsonNode errorNode = root.path("error");
      String accountErrorCode = errorNode.path("code").asText();

      PgErrorCode mappedErrorCode = PgErrorCode.fromPgErrorCode(accountErrorCode);
      log.info("pg error code: {}", mappedErrorCode);

      // 커스텀 예외로 throw
      throw new PgException(mappedErrorCode);

    }
  }
}
