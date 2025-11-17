package gighub.worketserver.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refresh_token")
public class UserRefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "refresh_token_id")
  private Long refreshTokenId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "refresh_token", nullable = false, length = 512)
  private String refreshToken;

  @Column(name = "issued_at")
  private LocalDateTime issuedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "is_revoked")
  private Boolean isRevoked;

  public void revoke() {
    this.isRevoked = true;
  }

  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !Boolean.TRUE.equals(isRevoked) && !isExpired();
  }

  public static UserRefreshToken create(Long userId, String refreshToken, LocalDateTime expiresAt) {
    UserRefreshToken token = new UserRefreshToken();
    token.userId = userId;
    token.refreshToken = refreshToken;
    token.issuedAt = LocalDateTime.now();
    token.expiresAt = expiresAt;
    token.isRevoked = false;
    return token;
  }
}
