package gighub.worketserver.controller;

import gighub.worketserver.dto.StatisticsResponse;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통계 관련 API Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

  private final StatisticsService statisticsService;

  /**
   * 통계 조회 (소득관리 탭)
   * GET /statistics
   */
  @GetMapping
  public ApiResponse<StatisticsResponse> getStatistics(Authentication authentication) {
    StatisticsResponse response = statisticsService.getStatistics(authentication);
    return ApiResponse.ok(response);
  }
}
