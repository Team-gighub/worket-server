package gighub.worketserver.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private Long id;
    private String name;
    private String provider;
    private String role;
    private String status;
    private String phone;
    private String createdAt;
}

