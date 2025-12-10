package gighub.worketserver.dto;

import lombok.Getter;

@Getter
public enum FreelancerIndustryEnum {
  WEB_APP_DEVELOPER("웹/앱 개발자"),
  DESIGNER("디자이너"),
  CONTENT_CREATOR("콘텐츠 제작자"),
  MC_HOST("MC/사회자"),
  TRANSLATOR("번역/통역가"),
  WRITER_EDITOR("작가/에디터");

  private final String field;

  FreelancerIndustryEnum(String field) {
    this.field = field;
  }

}
