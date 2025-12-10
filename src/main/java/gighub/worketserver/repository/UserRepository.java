package gighub.worketserver.repository;

import gighub.worketserver.domain.User;
import gighub.worketserver.dto.RoleCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByOauthId(String oauthId);

  // 1. 총 가입자 수 (Role별 집계)
  // role로 그룹화하여 전체 누적 가입자 수를 계산
  // 날짜 함수와 같은 통계 처리를 위해 Native Query를 사용
  @Query(value = """
    SELECT
        u.role AS role,
        COUNT(u.user_id) AS count
    FROM
        User u
    GROUP BY
        u.role
    """, nativeQuery = true)
  List<RoleCount> countTotalUsersByRole();

  // 2. 일일 신규 가입자 수 (Role별 집계)
  @Query(value = """
    SELECT
        u.role AS role,
        COUNT(u.user_id) AS count
    FROM
        User u
    WHERE
        DATE(u.created_at) = CURDATE()
    GROUP BY
        u.role
    """, nativeQuery = true)
  List<RoleCount> countDailyNewUsersByRole();

  // 3. 월간 신규 가입자 수
  // DATETIME/TIMESTAMP 필드는 DB에 따라 DATE_TRUNC('month', ...) 또는 DATE_FORMAT(..., '%Y-%m') 등을 사용해야 합니다.
  @Query(value = """
        SELECT
            u.role AS role,
            COUNT(u.user_id) AS count
        FROM
            User u
        WHERE
            DATE_FORMAT(u.created_at, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')
        GROUP BY
            u.role
    """, nativeQuery = true)
  List<RoleCount> countMonthlyNewUsersByRole();

}
