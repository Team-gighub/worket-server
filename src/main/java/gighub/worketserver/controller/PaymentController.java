package gighub.worketserver.controller;

import gighub.worketserver.dto.PasscodeDto;
import gighub.worketserver.dto.PaymentApprovalRequest;
import gighub.worketserver.dto.PaymentApprovalResponse;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.PaymentService;
import gighub.worketserver.service.SampleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
  private final PaymentService paymentService;

  @PostMapping("/approval")
  public ApiResponse<PaymentApprovalResponse> approval(@Valid @RequestBody PaymentApprovalRequest request) {

    return ApiResponse.ok(paymentService.approval(request.getTransactionId(),request.getEscrowId(), request.getConfirmToken()));
  }
}
