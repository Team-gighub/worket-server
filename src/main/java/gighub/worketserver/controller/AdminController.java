package gighub.worketserver.controller;

import gighub.worketserver.dto.AdminStatsResponse;
import gighub.worketserver.dto.ContractModificationDetail;
import gighub.worketserver.dto.ContractModificationResponse;
import gighub.worketserver.dto.ContractModifyRequest;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 페이지
 * 1. 계약서 수정 요청
 * 2. 사용자 통계 조회
 */
@RestController
@RequiredArgsConstructor
public class AdminController {
  private final AdminService adminService;

  @GetMapping("/contract-modifications")
  public ApiResponse<List<ContractModificationResponse>> getModificationList() {
    List<ContractModificationResponse> responseList = adminService.getModificationList();
    return ApiResponse.ok(responseList);
  }

  @GetMapping("/contract-modifications/{id}")
  public ApiResponse<ContractModificationDetail> getModificationById(@PathVariable Long id) {
    ContractModificationDetail response = adminService.getModificationById(id);
    return ApiResponse.ok(response);
  }

  @PostMapping("/contract-modifications/{id}/apply")
  public ApiResponse<String> applyModification(@PathVariable Long id, @RequestBody ContractModificationDetail requestDto) {
    adminService.applyModification(id, requestDto);
    return ApiResponse.ok("수정 성공");
  }

  @GetMapping("/user-statics")
  public ApiResponse<AdminStatsResponse> getAdminDashboardStats() {
    AdminStatsResponse stats = adminService.getDashboardStats();
    return ApiResponse.ok(stats);
  }
}


