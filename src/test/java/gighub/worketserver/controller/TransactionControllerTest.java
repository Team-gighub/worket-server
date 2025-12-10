package gighub.worketserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.global.exception.TransactionErrorCode;
import gighub.worketserver.global.exception.TransactionException;
import gighub.worketserver.global.filter.PasscodeCheckFilter;
import gighub.worketserver.global.filter.ProfileCheckFilter;
import gighub.worketserver.global.filter.TokenAuthenticationFilter;
import gighub.worketserver.global.security.handler.CustomAccessDeniedHandler;
import gighub.worketserver.global.security.handler.CustomAuthenticationEntryPoint;
import gighub.worketserver.global.security.handler.OAuth2SuccessHandler;
import gighub.worketserver.global.security.service.CustomOAuth2UserService;
import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.util.CookieUtil;
import gighub.worketserver.repository.FreelancerProfileRepository;
import gighub.worketserver.service.RefreshTokenService;
import gighub.worketserver.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TransactionController 단위 테스트
 * 
 * @WebMvcTest를 사용하여 Controller 레이어만 테스트
 * addFilters = false로 Spring Security 필터 체인 비활성화
 * 
 * 주의: 필터들이 @Component로 등록되어 있어 자동으로 로드되므로
 * 필터와 그 의존성들을 @MockBean으로 선언하여 빈 생성 오류 방지
 */
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TransactionController 단위 테스트")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // === Controller 의존성 ===
    @MockBean
    private TransactionService transactionService;

    // === 필터들 (Spring Bean으로 등록되어 있어서 MockBean 필요) ===
    @MockBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockBean
    private ProfileCheckFilter profileCheckFilter;

    @MockBean
    private PasscodeCheckFilter passcodeCheckFilter;

    // === 필터들의 의존성 ===
    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private CookieUtil cookieUtil;

    @MockBean
    private FreelancerProfileRepository freelancerProfileRepository;

    // === Security 관련 빈들 ===
    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    // 혹시 모를 추가 의존성 (Security Config에서 사용하는 경우)
    // 에러 발생 시 주석 해제
    // @MockBean
    // private CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
    // 
    // @MockBean
    // private CustomAuthorizationRequestRepository customAuthorizationRequestRepository;

    // 테스트용 Authentication 객체 생성 헬퍼 메서드
    private Authentication createAuthentication(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null, Collections.emptyList());
    }

    /**
     * 테스트 1: GET /transactions - 거래 목록 조회 (year, month 파라미터 있음)
     */
    @Test
    @DisplayName("year와 month 파라미터가 주어진 경우 거래 목록을 조회해야 한다")
    void getTransactions_WithYearAndMonth_Success() throws Exception {
        // Given
        Long userId = 1L;
        Integer year = 2024;
        Integer month = 11;
        Authentication authentication = createAuthentication(userId);

        // 테스트 데이터 생성
        TransactionSummaryDto transaction1 = TransactionSummaryDto.builder()
                .transactionId(1L)
                .title("웹사이트 개발 프로젝트")
                .status("COMPLETED")
                .amount(new BigDecimal("5000000"))
                .startDate("2024-11-01")
                .endDate("2024-11-30")
                .build();

        TransactionSummaryDto transaction2 = TransactionSummaryDto.builder()
                .transactionId(2L)
                .title("모바일 앱 개발")
                .status("IN_PROGRESS")
                .amount(new BigDecimal("3000000"))
                .startDate("2024-11-15")
                .endDate("2024-12-15")
                .build();

        StatusCountDto statusCount1 = new StatusCountDto("COMPLETED", 1);
        StatusCountDto statusCount2 = new StatusCountDto("IN_PROGRESS", 1);

        TransactionListResponse response = TransactionListResponse.builder()
                .freelancerName("홍길동")
                .totalAmount("8000000")
                .statusCounts(Arrays.asList(statusCount1, statusCount2))
                .contractList(Arrays.asList(transaction1, transaction2))
                .build();

        given(transactionService.getTransactions(any(Authentication.class), eq(year), eq(month)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions")
                        .param("year", year.toString())
                        .param("month", month.toString())
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.freelancerName").value("홍길동"))
                .andExpect(jsonPath("$.data.totalAmount").value("8000000"))
                .andExpect(jsonPath("$.data.contractList").isArray())
                .andExpect(jsonPath("$.data.contractList.length()").value(2))
                .andExpect(jsonPath("$.data.contractList[0].transactionId").value(1))
                .andExpect(jsonPath("$.data.contractList[0].title").value("웹사이트 개발 프로젝트"))
                .andExpect(jsonPath("$.data.contractList[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.statusCounts.length()").value(2));
    }

    /**
     * 테스트 2: GET /transactions - 거래 목록 조회 (파라미터 없음, 기본값 사용)
     */
    @Test
    @DisplayName("year와 month 파라미터가 없는 경우 기본값으로 거래 목록을 조회해야 한다")
    void getTransactions_WithoutParameters_Success() throws Exception {
        // Given
        Long userId = 1L;
        Authentication authentication = createAuthentication(userId);

        TransactionListResponse response = TransactionListResponse.builder()
                .freelancerName("김철수")
                .totalAmount("2000000")
                .statusCounts(Collections.singletonList(new StatusCountDto("COMPLETED", 1)))
                .contractList(Collections.singletonList(
                        TransactionSummaryDto.builder()
                                .transactionId(3L)
                                .title("디자인 작업")
                                .status("COMPLETED")
                                .amount(new BigDecimal("2000000"))
                                .startDate("2024-12-01")
                                .endDate("2024-12-10")
                                .build()
                ))
                .build();

        given(transactionService.getTransactions(any(Authentication.class), isNull(), isNull()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions")
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.freelancerName").value("김철수"))
                .andExpect(jsonPath("$.data.totalAmount").value("2000000"));
    }

    /**
     * 테스트 3: GET /transactions - 빈 거래 목록 반환
     */
    @Test
    @DisplayName("거래 내역이 없는 경우 빈 목록을 반환해야 한다")
    void getTransactions_EmptyList_Success() throws Exception {
        // Given
        Long userId = 1L;
        Authentication authentication = createAuthentication(userId);

        TransactionListResponse response = TransactionListResponse.builder()
                .freelancerName("이영희")
                .totalAmount("0")
                .statusCounts(Collections.emptyList())
                .contractList(Collections.emptyList())
                .build();

        given(transactionService.getTransactions(any(Authentication.class), anyInt(), anyInt()))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions")
                        .param("year", "2024")
                        .param("month", "10")
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.contractList").isEmpty());
    }

    /**
     * 테스트 4: GET /transactions/{transactionId}/preview - 거래 미리보기 (인증 불필요)
     */
    @Test
    @DisplayName("거래 미리보기를 요청했을 때 인증 없이 정상 조회되어야 한다")
    void getTransactionPreview_Success() throws Exception {
        // Given
        Long transactionId = 1L;
        
        TransactionPreviewResponse response = TransactionPreviewResponse.builder()
                .title("웹사이트 리뉴얼 프로젝트")
                .freelancerName("박민수")
                .clientName("(주)테크컴퍼니")
                .build();

        given(transactionService.getTransactionPreview(transactionId))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}/preview", transactionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.title").value("웹사이트 리뉴얼 프로젝트"))
                .andExpect(jsonPath("$.data.freelancerName").value("박민수"))
                .andExpect(jsonPath("$.data.clientName").value("(주)테크컴퍼니"));
    }

    /**
     * 테스트 5: GET /transactions/{transactionId}/preview - 존재하지 않는 거래
     */
    @Test
    @DisplayName("존재하지 않는 거래 ID로 미리보기를 요청했을 때 404 에러를 반환해야 한다")
    void getTransactionPreview_NotFound() throws Exception {
        // Given
        Long transactionId = 999L;
        
        given(transactionService.getTransactionPreview(transactionId))
                .willThrow(new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}/preview", transactionId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * 테스트 6: GET /transactions/{transactionId}/permissions - 프리랜서 권한 확인
     */
    @Test
    @DisplayName("프리랜서 권한으로 접근권한을 확인했을 때 정상 응답되어야 한다")
    void checkTransactionPermission_FreelancerRole_Success() throws Exception {
        // Given
        Long userId = 1L;
        Long transactionId = 1L;
        Authentication authentication = createAuthentication(userId);

        TransactionPermissionResponse response = TransactionPermissionResponse.builder()
                .userRole("FREELANCER")
                .permission(true)
                .build();

        given(transactionService.checkPermission(any(Authentication.class), eq(transactionId)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}/permissions", transactionId)
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userRole").value("FREELANCER"))
                .andExpect(jsonPath("$.data.permission").value(true));
    }

    /**
     * 테스트 7: GET /transactions/{transactionId}/permissions - 클라이언트 권한 확인
     */
    @Test
    @DisplayName("클라이언트 권한으로 접근권한을 확인했을 때 정상 응답되어야 한다")
    void checkTransactionPermission_ClientRole_Success() throws Exception {
        // Given
        Long userId = 2L;
        Long transactionId = 1L;
        Authentication authentication = createAuthentication(userId);

        TransactionPermissionResponse response = TransactionPermissionResponse.builder()
                .userRole("CLIENT")
                .permission(true)
                .build();

        given(transactionService.checkPermission(any(Authentication.class), eq(transactionId)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}/permissions", transactionId)
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userRole").value("CLIENT"))
                .andExpect(jsonPath("$.data.permission").value(true));
    }

    /**
     * 테스트 8: GET /transactions/{transactionId}/permissions - 접근 권한 없음
     */
    @Test
    @DisplayName("접근 권한이 없는 경우 403 에러를 반환해야 한다")
    void checkTransactionPermission_AccessDenied() throws Exception {
        // Given
        Long userId = 3L;
        Long transactionId = 1L;
        Authentication authentication = createAuthentication(userId);

        given(transactionService.checkPermission(any(Authentication.class), eq(transactionId)))
                .willThrow(new TransactionException(TransactionErrorCode.TRANSACTION_ACCESS_DENIED));

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}/permissions", transactionId)
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    /**
     * 테스트 9: GET /transactions/{transactionId} - 거래 상세 조회 성공
     */
    @Test
    @DisplayName("거래 상세를 조회했을 때 정상 조회되어야 한다")
    void getTransactionDetail_Success() throws Exception {
        // Given
        Long userId = 1L;
        Long transactionId = 1L;
        Authentication authentication = createAuthentication(userId);

        ContractInfoDto contractInfo = ContractInfoDto.builder()
                .title("웹사이트 개발")
                .amount(new BigDecimal("5000000"))
                .startDate("2024-11-01")
                .endDate("2024-11-30")
                .build();

        ClientInfoDto clientInfo = ClientInfoDto.builder()
                .name("(주)테크컴퍼니")
                .phone("010-1234-5678")
                .build();

        FreelancerInfoDto freelancerInfo = FreelancerInfoDto.builder()
                .name("홍길동")
                .phone("010-9876-5432")
                .account("123-456-789012")
                .bank("국민은행")
                .build();

        TransactionDetailResponse response = TransactionDetailResponse.builder()
                .status("COMPLETED")
                .signedAt("2024-11-01T10:00:00")
                .depositHoldAt("2024-11-01T11:00:00")
                .paymentConfirmedAt("2024-11-30T15:00:00")
                .settledAt("2024-11-30T16:00:00")
                .createdAt("2024-10-25T09:00:00")
                .contractId(1L)
                .settledAmount(new BigDecimal("4500000"))
                .contractFileUrl("https://s3.amazonaws.com/contracts/contract.pdf")
                .contractInfo(contractInfo)
                .clientInfo(clientInfo)
                .freelancerInfo(freelancerInfo)
                .build();

        given(transactionService.getTransactionDetail(any(Authentication.class), eq(transactionId)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}", transactionId)
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.contractId").value(1))
                .andExpect(jsonPath("$.data.contractInfo.title").value("웹사이트 개발"))
                .andExpect(jsonPath("$.data.contractInfo.amount").value(5000000))
                .andExpect(jsonPath("$.data.clientInfo.name").value("(주)테크컴퍼니"))
                .andExpect(jsonPath("$.data.freelancerInfo.name").value("홍길동"))
                .andExpect(jsonPath("$.data.freelancerInfo.bank").value("국민은행"));
    }

    /**
     * 테스트 10: GET /transactions/{transactionId} - 존재하지 않는 거래
     */
    @Test
    @DisplayName("존재하지 않는 거래 ID로 상세 조회를 요청했을 때 404 에러를 반환해야 한다")
    void getTransactionDetail_NotFound() throws Exception {
        // Given
        Long userId = 1L;
        Long transactionId = 999L;
        Authentication authentication = createAuthentication(userId);

        given(transactionService.getTransactionDetail(any(Authentication.class), eq(transactionId)))
                .willThrow(new RestApiException(CommonErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}", transactionId)
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * 테스트 11: GET /transactions/{transactionId} - 접근 권한 없음
     */
    @Test
    @DisplayName("접근 권한 없이 거래 상세를 조회했을 때 403 에러를 반환해야 한다")
    void getTransactionDetail_Forbidden() throws Exception {
        // Given
        Long userId = 3L;
        Long transactionId = 1L;
        Authentication authentication = createAuthentication(userId);

        given(transactionService.getTransactionDetail(any(Authentication.class), eq(transactionId)))
                .willThrow(new RestApiException(CommonErrorCode.FORBIDDEN_ACCESS, "거래에 대한 접근 권한이 없습니다."));

        // When & Then
        mockMvc.perform(get("/transactions/{transactionId}", transactionId)
                        .principal(authentication))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
