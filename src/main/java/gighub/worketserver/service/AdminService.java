package gighub.worketserver.service;

import gighub.worketserver.domain.*;
import gighub.worketserver.domain.constants.ModifyStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static gighub.worketserver.service.TransactionService.DATE_FORMATTER;

/**
 * 관리자 페이지 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
  private final TransactionRepository transactionRepository;
  private final ContractModifyRepository contractModifyRepository;
  private final ContractRepository contractRepository;
  private final UserRepository userRepository;
  private final FreelancerProfileRepository freelancerProfileRepository;
  private final ContractFileRepository contractFileRepository;

  /**
   * DB 집계 결과(RoleCount 리스트)를 최종 응답 DTO로 변환
   *
   * @param roleCounts Repository에서 받은 role별 count
   * @return all, freelancer, client 수가 계산된 RoleStats DTO
   */
  private RoleStats mapToRoleStats(List<RoleCount> roleCounts) {
    long freelancerCount = 0;
    long clientCount = 0;
    long totalCount = freelancerCount + clientCount;

    for (RoleCount rc : roleCounts) {
      String role = rc.getRole();
      Long count = rc.getCount();

      if ("FREELANCER".equals(role)) {
        freelancerCount = count;
      } else if ("CLIENT".equals(role)) {
        clientCount = count;
      }
    }
    return RoleStats.builder()
      .freelancer(freelancerCount)
      .client(clientCount)
      .all(totalCount)
      .build();
  }

  /**
   * DB 집계 결과(FreelancerExperience)를 최종 응답 DTO로 변환
   *
   * @return List<FreelancerExperience> Enum변환과 count가 들어간 최종 DTO
   * @params List<FreelancerExperienceCounts> 업력에 따른 count
   */
  private List<FreelancerExperience> mapToExperienceStats(List<FreelancerExperienceCount> counts) {

    // 1. ENUM을 Map으로 변환 (키: 이름, 값: level 문자열)
    Map<String, String> experienceMap = Arrays.stream(FreelancerExperienceEnum.values())
      .collect(Collectors.toMap(Enum::name, FreelancerExperienceEnum::getLevel));

    // 2. 쿼리 결과를 새로운 DTO 리스트로 변환
    return counts.stream()
      .map(item -> {
        String level = experienceMap.get(item.getName()); // DB 이름으로 level 매핑
        if (level != null) {
          return FreelancerExperience.builder()
            .level(level)
            .count(item.getCount().intValue())
            .build();
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * DB 집계 결과(FreelancerIndustry)를 최종 응답 DTO로 변환
   *
   * @return List<FreelancerIndustry> Enum변환과 count가 들어간 최종 DTO
   * @params List<FreelancerExperienceCounts> 업력에 따른 count
   */
  private List<FreelancerIndustry> mapToIndustryStats(List<FreelancerExperienceCount> counts) {

    // 1. ENUM을 Map으로 변환 (키: field, 값: field 자체)
    Map<String, String> industryMap = Arrays.stream(FreelancerIndustryEnum.values())
      .collect(Collectors.toMap(FreelancerIndustryEnum::getField, FreelancerIndustryEnum::getField));

    // 2. 쿼리 결과를 새로운 DTO 리스트로 변환
    return counts.stream()
      .map(item -> {
        String field = industryMap.get(item.getName());
        if (field != null) {
          return FreelancerIndustry.builder()
            .field(field)
            .count(item.getCount().intValue())
            .build();
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }


  /**
   * 수정 계약서 요청
   */
  public void createModification(Authentication authentication, ContractModifyRequest request, Long id) {
    Transaction transaction = transactionRepository.findById(id).
      orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "거래 내역이 없습니다."));
    Long userId = Long.parseLong(authentication.getName());// User 조회
    User freelancer = userRepository.findById(userId)
      .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "유저 정보가 존재하지 않습니다."));
    ContractModification contractModification = ContractModification.builder()
      .transaction(transaction)
      .userName(freelancer.getName())
      .status(ModifyStatus.PENDING)
      .createdAt(LocalDateTime.now())
      .content(request.getContent())
      .build();
    contractModifyRepository.save(contractModification);
  }

  /**
   * 수정 계약서 조회
   */
  public List<ContractModificationResponse> getModificationList() {
    List<ContractModification> contractModificationList = contractModifyRepository.findAllWithTransaction();
    // 2. Stream을 사용하여 리스트를 즉시 변환합니다.
    List<ContractModificationResponse> responseList = contractModificationList.stream()
      .map(entity -> ContractModificationResponse.builder()
        // Long 타입 ID를 그대로 사용
        .modificationId(entity.getId())

        // Transaction 엔티티에서 ID를 가져옵니다. (Lazy Loading 주의 필요)
        .transactionId(entity.getTransaction().getId())

        .userName(entity.getUserName())
        // ENUM을 문자열로 변환 (.name() 사용)
        .status(entity.getStatus().name())

        // LocalDateTime 사용
        .createdAt(entity.getCreatedAt())
        .build())
      .collect(Collectors.toList());
    return responseList;
  }

  /**
   * 수정 계약서 단건 조회
   */
  public ContractModificationDetail getModificationById(Long id) {
    ContractModification contractModification = contractModifyRepository.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
    Transaction transaction = transactionRepository.findById(contractModification.getTransaction().getId()).orElseThrow(() -> new RuntimeException("Transaction not found"));
    Contract contract = contractRepository.findById(transaction.getContract().getId()).orElseThrow(() -> new RuntimeException("Contract not found"));
    ContractFile contractFile = contractFileRepository.findByContractId(contract.getId());
    String fileUrl = contractFile.getFileUrl();
    //사용자 정보
    ClientInfoDto clientInfoDto = ClientInfoDto.builder()
      .name(contract.getClient().getName())
      .phone(contract.getClient().getPhone())
      .build();

    //프리랜서 정보
    FreelancerInfoDto freelancerInfoDto = FreelancerInfoDto.builder()
      .name(contract.getFreelancer().getName())
      .phone(contract.getFreelancer().getPhone())
      .account(transaction.getFreelancerAccount())
      .bank(transaction.getFreelancerBank())
      .build();

    //계약정보
    ContractInfoDto contractInfoDto = ContractInfoDto.builder()
      .title(contract.getTitle())
      .amount(contract.getAmount())
      .startDate(contract.getStartDate() != null ? contract.getStartDate().format(DATE_FORMATTER) : null)
      .endDate(contract.getEndDate() != null ? contract.getEndDate().format(DATE_FORMATTER) : null)
      .build();

    return ContractModificationDetail.builder()
      .clientInfoDto(clientInfoDto)
      .freelancerInfoDto(freelancerInfoDto)
      .contractInfoDto(contractInfoDto)
      .content(contractModification.getContent())
      .fileUrl(fileUrl)
      .build();
  }

  /**
   * 수정 계약서 변경 적용
   */
  public void applyModification(Long id, ContractModificationDetail detail) {
    ContractModification contractModification = contractModifyRepository.findWithTransactionAndContractById(id)
      .orElseThrow(() -> new RuntimeException("수정 요청을 찾을 수 없습니다.")); // 예외 메시지 수정 권장    Transaction transaction = transactionRepository.findById(modify.getTransaction().getId()).orElseThrow(() -> new RuntimeException("Transaction not found"));
    Transaction transaction = contractModification.getTransaction();
    Contract contract = contractModification.getTransaction().getContract();
    //프리랜서 정보 변경
    transaction.updateFreelancerInfo(detail.getFreelancerInfoDto());
    // 클라이언트 정보 변경
    contract.updateClientInfo(detail.getClientInfoDto());
    // 계약 정보 변경
    contract.updateContractInfo(detail.getContractInfoDto());

    // 상태 변경
    contractModification.updateStatus(ModifyStatus.APPROVED);

  }

  public AdminStatsResponse getDashboardStats() {

    //1. UserStatics 정보 입력
    //1-1. repository에서 받아온 값들 매핑
    List<RoleCount> totalCounts = userRepository.countTotalUsersByRole();
    List<RoleCount> dailyCounts = userRepository.countDailyNewUsersByRole();
    List<RoleCount> monthlyCounts = userRepository.countMonthlyNewUsersByRole();

    RoleStats totalStats = mapToRoleStats(totalCounts);
    RoleStats dailyStats = mapToRoleStats(dailyCounts);
    RoleStats monthlyStats = mapToRoleStats(monthlyCounts);

    UserStatics userStats = UserStatics.builder()
      .totalUsers(totalStats)
      .dailyNewUsers(dailyStats)
      .monthlyNewUsers(monthlyStats)
      .build();

    //2. Freelancer 업종, 업력 받아오기
    List<FreelancerExperienceCount> experienceCounts = freelancerProfileRepository.countByExperienceLevel();
    List<FreelancerExperience> experienceList = mapToExperienceStats(experienceCounts);
    List<FreelancerExperienceCount> industryCounts = freelancerProfileRepository.countByIndustry();
    List<FreelancerIndustry> industryList = mapToIndustryStats(industryCounts);
    FreelancerDetail freelancerDetail = FreelancerDetail.builder()
      .industry(industryList)
      .experience(experienceList)
      .build();

    return AdminStatsResponse.builder()
      .userStatics(userStats)
      .freelancerDetail(freelancerDetail)
      .build();
  }
}
