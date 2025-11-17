package gighub.worketserver.dto;

import gighub.worketserver.domain.constants.Provider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
  private Long id;
  private String name;
  private Provider provider;
  private String role;
  private String status;
  private String phone;
  private String createdAt;
}
