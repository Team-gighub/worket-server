package gighub.worketserver.client;

import gighub.worketserver.dto.*;
import gighub.worketserver.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-system", url = "${external.api.account-system.url}")
public interface CoreApiClient {

  @PostMapping("/api/v1/payment/approval")
  ApiResponse<CoreApprovalResponse> requestPaymentApproval(@RequestBody CoreApprovalRequest request);

  @PostMapping("/api/v1/payment/confirm")
  ApiResponse<CoreConfirmResponse> requestPaymentConfirm(@RequestBody CoreConfirmRequest request);

}
