package gighub.worketserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatics {
  private RoleStats totalUsers;
  private RoleStats dailyNewUsers;
  private RoleStats monthlyNewUsers;
}
