package gighub.worketserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DB 집계 결과를 담는 DTO
 * 쿼리 결과 매핑을 위해 필요
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCount {
  private String role;
  private Long count;
}
