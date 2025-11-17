package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "oauth_token")
public class OauthToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "token_id")
  private Long tokenId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false)
  private Provider provider;

  @Column(name = "access_token", nullable = false, length = 512)
  private String accessToken;

  @Column(name = "refresh_token", length = 512)
  private String refreshToken;

  @Column(name = "refresh_expires_at")
  private LocalDateTime refreshExpiresAt;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void updateTokens(String accessToken, String refreshToken, LocalDateTime refreshExpiresAt) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.refreshExpiresAt = refreshExpiresAt;
  }

  public void updateAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshTokenExpiresAt(LocalDateTime refreshExpiresAt) {
    this.refreshExpiresAt = refreshExpiresAt;
  }
}
