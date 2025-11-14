package gighub.worketserver.controller;

import gighub.worketserver.dto.ContractCreateRequest;
import gighub.worketserver.dto.ContractCreateResponse;
import gighub.worketserver.dto.ContractExtractResponse;
import gighub.worketserver.dto.SignatureRequest;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 계약서 관련 API Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;

    /**
     * 계약서 추출 (OCR + LLM)
     * POST /contracts/extract
     */
    @PostMapping("/extract")
    public ApiResponse<ContractExtractResponse> extractContract(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        ContractExtractResponse response = contractService.extractContract(authentication, file);
        return ApiResponse.ok(response);
    }

    /**
     * 계약서 등록
     * POST /contracts
     * @return 201 Created // ResponseEntity로 상태코드를 명시하면 더 RESTful
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ContractCreateResponse> createContract(
            Authentication authentication,
            @RequestBody ContractCreateRequest request
    ) {
        ContractCreateResponse response = contractService.createContract(authentication, request);
        return ApiResponse.ok(response);
    }

    /**
     * 서명 등록
     * POST /contracts/{contractId}/signatures
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
