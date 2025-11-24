package gighub.worketserver.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

@Getter
public class PasscodeDto {
  @NotBlank(message = "패스코드는 빈칸일 수 없습니다.")
  // 숫자 6개
  @Pattern(regexp = "^\\d{6}$", message = "패스코드는 숫자 6자리 입니다.")
  private String passcode;
}
