# Worket Server - CRUD 스켈레톤 구현 완료

## 📋 구현된 엔드포인트 목록

### 1. 인증 (Auth) - `AuthController`
- `POST /auth/logout` - 로그아웃
- `POST /auth/token/refresh` - 카카오 토큰 재발급
- `POST /auth/unlink` - 회원 탈퇴
- `POST /auth/passcode/register` - 간편 비밀번호 등록 ⭐ **201 Created**
- `POST /auth/passcode/verify` - 간편 비밀번호 검증

### 2. 사용자 (User) - `UserController`
- `GET /users` - 전체 유저 조회 (관리자용)
- `GET /mypage` - 마이페이지 조회
- `GET /users/{userId}` - 유저 정보 조회
- `POST /users/{userId}` - 유저 정보 수정

### 3. 계약서 (Contract) - `ContractController`
- `POST /contracts/extract` - 계약서 추출 (OCR + LLM)
- `POST /contracts` - 계약서 등록 ⭐ **201 Created**
- `POST /contracts/{contractId}/signatures` - 서명 등록 ⭐ **201 Created**

### 4. 거래 (Transaction) - `TransactionController`
- `GET /transactions` - 거래 전체 조회 (월별)
- `GET /transactions/{transactionId}/preview` - 거래 정보 미리보기 (인증 불필요)
- `GET /transactions/{transactionId}/permissions` - 거래 접근권한 판단
- `GET /transactions/{transactionId}` - 거래 상세 조회

### 5. 통계 (Statistics) - `StatisticsController`
- `GET /statistics` - 통계 조회 (소득관리 탭)

---

## 📁 생성/수정된 파일 목록

### Controllers (5개)
✅ `controller/AuthController.java` - **NEW**
✅ `controller/ContractController.java` - **NEW**
✅ `controller/TransactionController.java` - **NEW**
✅ `controller/StatisticsController.java` - **NEW**
✅ `controller/UserController.java` - **UPDATED**

### Services (5개)
✅ `service/AuthService.java` - **NEW**
✅ `service/ContractService.java` - **NEW**
✅ `service/TransactionService.java` - **NEW**
✅ `service/StatisticsService.java` - **NEW**
✅ `service/UserService.java` - **UPDATED**

### DTOs (6개 수정)
✅ `dto/SignatureRequest.java` - **NEW**
✅ `dto/UserDetailDto.java` - **UPDATED** (타입 변경: Enum → String)
✅ `dto/UserUpdateRequest.java` - **UPDATED** (필드 추가)
✅ `dto/UserUpdateDto.java` - **UPDATED** (타입 변경)
✅ `dto/MonthlyStatisticsDto.java` - **UPDATED** (Long → BigDecimal)
✅ `dto/YearProfitDto.java` - **UPDATED** (Long → Integer)

### Domain Entities (2개 수정)
✅ `domain/Transaction.java` - **UPDATED** (updateStatus 메서드 추가)
✅ `domain/Passcode.java` - **UPDATED** (필드 구조 변경)

---

## 🎯 주요 설계 특징

### 1. **ApiResponse 통일**
- 모든 응답은 `ApiResponse<T>`로 래핑
- ResponseEntity 사용이 더 적합한 경우 주석으로 표시

```java
// ResponseEntity로 상태코드를 명시하면 더 RESTful
@PostMapping("/passcode/register")
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<Void> registerPasscode(...)
```

### 2. **명확한 레이어 분리**
```
Controller → Service → Repository → Domain
         ↓
        DTO
```

- **Controller**: HTTP 요청/응답 처리, 인증 객체 추출
- **Service**: 비즈니스 로직, 트랜잭션 관리 (목데이터 반환)
- **Repository**: 데이터베이스 접근
- **Domain**: 엔티티, 비즈니스 규칙
- **DTO**: 계층 간 데이터 전송

### 3. **목(Mock) 데이터 제공**
- 모든 Service는 실제 DB 대신 목 데이터 반환
- 즉시 테스트 가능
- 실제 구현 시 `// TODO:` 주석 참고

### 4. **인증 처리**
- `Authentication authentication` 파라미터로 JWT 토큰에서 사용자 ID 추출
- Security Context 활용

---

## ⭐ ResponseEntity 사용 권장 케이스 (주석 표시됨)

다음 상황에서는 `ResponseEntity`를 사용하는 것이 더 RESTful합니다:

1. **201 Created**: 리소스 생성 시
   - `POST /auth/passcode/register`
   - `POST /contracts`
   - `POST /contracts/{contractId}/signatures`

2. **204 No Content**: 성공했지만 반환할 내용이 없을 때
3. **302 Redirect**: 리다이렉션이 필요할 때
4. **커스텀 헤더**: Location 헤더 등 추가가 필요할 때

현재는 `@ResponseStatus(HttpStatus.CREATED)`로 처리했지만,
더 세밀한 제어가 필요하면 `ResponseEntity`로 변경 가능합니다.

---

## 🚀 다음 단계 (구현 가이드)

### 1. Service 로직 구현
```java
// Mock 데이터를 실제 Repository 호출로 교체
public TransactionListResponse getTransactions(...) {
    // Mock: 거래 목록 생성
    // TODO: 실제 DB 조회
    List<Transaction> transactions = transactionRepository
        .findByFreelancerIdAndYearAndMonth(userId, year, month);
    // ...
}
```

### 2. 예외 처리 강화
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Void> handleNotFound(EntityNotFoundException e) {
        return ApiResponse.error(e.getMessage());
    }
}
```

### 3. Validation 추가
```java
public ApiResponse<ContractCreateResponse> createContract(
    @Valid @RequestBody ContractCreateRequest request
) {
    // ...
}
```

### 4. Repository 커스텀 쿼리
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFreelancerIdAndCreatedAtBetween(
        Long freelancerId, 
        LocalDateTime start, 
        LocalDateTime end
    );
}
```

### 5. 테스트 코드
```java
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {
    @Test
    void 거래_목록_조회_테스트() {
        // given, when, then
    }
}
```

---

## 📌 중요 참고사항

1. **OpenAPI 명세 100% 준수**: 모든 엔드포인트는 `Default module.openapi.json` 기준
2. **Lombok 활용**: `@RequiredArgsConstructor`, `@Slf4j` 등으로 코드 간소화
3. **트랜잭션 관리**: `@Transactional` 어노테이션으로 데이터 일관성 보장
4. **로깅**: 각 주요 메서드에 로그 추가로 디버깅 용이

---

## 💡 Tips

- **ApiResponse vs ResponseEntity**: 
  - 단순 성공/실패: `ApiResponse` ✅
  - 상태코드 제어 필요: `ResponseEntity` + 주석 참고

- **인증 없는 엔드포인트**: 
  - `/transactions/{transactionId}/preview`는 토큰 없이 접근 가능
  - SecurityConfig에서 `permitAll()` 설정 필요

- **파일 업로드**: 
  - `MultipartFile` 파라미터 사용
  - S3 등 스토리지 서비스 연동 필요

---

## ✅ 완료 체크리스트

- [x] Controller 5개 생성/수정
- [x] Service 5개 생성/수정
- [x] DTO 6개 생성/수정
- [x] Domain Entity 2개 수정
- [x] OpenAPI 명세 준수
- [x] ApiResponse 통일
- [x] ResponseEntity 권장 케이스 주석 표시
- [x] 목 데이터 제공
- [x] TODO 주석 추가
- [x] 로깅 추가

---

**모든 파일이 `C:\woori_workspace\crud\worket-server` 프로젝트에 생성/수정되었습니다!** 🎉
