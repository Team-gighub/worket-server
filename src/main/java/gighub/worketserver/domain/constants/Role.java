package gighub.worketserver.domain.constants;

public enum Role {
  FREELANCER("프리랜서"),
  CLIENT("의뢰인"),
  ADMIN("관리자");

  private final String korName;

  Role(String korName) {
    this.korName = korName;
  }

  public String getKorName() {
    return korName;
  }
}
