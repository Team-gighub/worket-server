package gighub.worketserver.controller;

import gighub.worketserver.dto.SignatureRequest;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 계약서 관련 API Controller
 */
@RequestMapping("/contracts")
@RestController
@RequiredArgsConstructor
public class ContractController {

  private final ContractService contractService;

  /**
   * 계약서 추출 (OCR + LLM)
   * POST /contracts/extract
   */
  @PostMapping("/extract")
  public ApiResponse<?> extractContract(
    @RequestPart("file") MultipartFile file,
    @RequestPart("message") String message
  ) {
    //TODO : 에러 처리 리팩토링 예정
    return contractService.extractContract(file, message);
  }

  /**
   * 서명 등록
   * POST /contracts/{contractId}/signatures
   *
   * @return 201 Created // ResponseEntity로 상태코드를 명시하면 더 RESTful
   */
  @PostMapping("/{contractId}/signatures")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> registerSignature(
    Authentication authentication,
    @PathVariable Long contractId,
    @RequestBody SignatureRequest request
  ) {
    contractService.registerSignature(authentication, contractId, request);
    return ApiResponse.ok(null);
  }
}
