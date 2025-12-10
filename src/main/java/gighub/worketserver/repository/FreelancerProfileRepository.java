package gighub.worketserver.repository;

import gighub.worketserver.domain.FreelancerProfile;
import gighub.worketserver.dto.FreelancerExperienceCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long> {

  Optional<FreelancerProfile> findByUserId(Long userId);

  //업력 별 조회를 위한 쿼리
  @Query(value = """
    SELECT
        CASE
            WHEN fp.business_sector_years = 0 THEN 'UNDER_1_YEAR'
            WHEN fp.business_sector_years BETWEEN 1 AND 2 THEN 'ONE_TO_TWO_YEARS'
            WHEN fp.business_sector_years BETWEEN 2 AND 3 THEN 'TWO_TO_THREE_YEARS'
            WHEN fp.business_sector_years BETWEEN 3 AND 5 THEN 'THREE_TO_FIVE_YEARS'
            WHEN fp.business_sector_years BETWEEN 5 AND 8 THEN 'FIVE_TO_EIGHT_YEARS'
            WHEN fp.business_sector_years >= 8 THEN 'EIGHT_PLUS_YEARS'
            ELSE 'OTHER'
        END AS name,
        COUNT(fp.user_id) AS count
    FROM
        freelancer_profile fp
    GROUP BY
        name
    """, nativeQuery = true)
  List<FreelancerExperienceCount> countByExperienceLevel();

  @Query(value = """
    SELECT
        fp.business_sector AS name,
        COUNT(fp.user_id) AS count
    FROM
        freelancer_profile fp
    WHERE
        fp.business_sector IN ('웹/앱 개발자', '디자이너', '콘텐츠 제작자', 'MC/사회자', '번역/통역가', '작가/에디터')
    GROUP BY
        fp.business_sector
    """, nativeQuery = true)
  List<FreelancerExperienceCount> countByIndustry();
}
