package gighub.worketserver.controller;

import gighub.worketserver.dto.TransactionDetailResponse;
import gighub.worketserver.dto.TransactionListResponse;
import gighub.worketserver.dto.TransactionPermissionResponse;
import gighub.worketserver.dto.TransactionPreviewResponse;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 거래(Transaction) 관련 API Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 거래 전체 조회 (월별)
     * GET /transactions?year=2025&month=11
     */
    @GetMapping
    public ApiResponse<TransactionListResponse> getTransactions(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        TransactionListResponse response = transactionService.getTransactions(authentication, year, month);
        return ApiResponse.ok(response);
    }

    /**
     * 거래 정보 미리보기 (토큰 없이 접근 가능)
     * GET /transactions/{transactionId}/preview
     */
    @GetMapping("/{transactionId}/preview")
    public ApiResponse<TransactionPreviewResponse> getTransactionPreview(@PathVariable Long transactionId) {
        TransactionPreviewResponse response = transactionService.getTransactionPreview(transactionId);
        return ApiResponse.ok(response);
    }

    /**
     * 거래 접근권한 판단
     * GET /transactions/{transactionId}/permissions
     */
    @GetMapping("/{transactionId}/permissions")
    public ApiResponse<TransactionPermissionResponse> checkTransactionPermission(
            Authentication authentication,
            @PathVariable Long transactionId
    ) {
        TransactionPermissionResponse response = transactionService.checkPermission(authentication, transactionId);
        return ApiResponse.ok(response);
    }

    /**
     * 거래 상세 조회
     * GET /transactions/{transactionId}
     */
    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionDetailResponse> getTransactionDetail(Authentication authentication, @PathVariable Long transactionId) {
        TransactionDetailResponse response = transactionService.getTransactionDetail(
          authentication,
          transactionId);
        return ApiResponse.ok(response);
    }
}
