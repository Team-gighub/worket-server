package gighub.worketserver.dto;

import lombok.Getter;

@Getter
public enum FreelancerExperienceEnum {
  UNDER_1_YEAR("1년 미만"),
  ONE_TO_TWO_YEARS("1년 이상 ~ 2년 미만"),
  TWO_TO_THREE_YEARS("2년 이상 ~ 3년 미만"),
  THREE_TO_FIVE_YEARS("3년 이상 ~ 5년 미만"),
  FIVE_TO_EIGHT_YEARS("5년 이상 ~ 8년 미만"),
  EIGHT_PLUS_YEARS("8년 이상");

  private final String level;

  FreelancerExperienceEnum(String level) {
    this.level = level;
  }

}
