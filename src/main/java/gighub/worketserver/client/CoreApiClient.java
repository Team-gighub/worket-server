package gighub.worketserver.client;

import gighub.worketserver.dto.CoreApprovalRequest;
import gighub.worketserver.dto.CoreConfirmRequest;
import gighub.worketserver.dto.CoreConfirmResponse;
import gighub.worketserver.dto.PaymentApprovalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "account-core-client",
  url = "${core.account.base-url}"
)
public interface CoreApiClient {

  @PostMapping("/payment/approval")
  PaymentApprovalResponse requestPaymentApproval(@RequestBody CoreApprovalRequest request);

  @PostMapping("/payment/confirm")
  CoreConfirmResponse requestPaymentConfirm(@RequestBody CoreConfirmRequest request);

}
