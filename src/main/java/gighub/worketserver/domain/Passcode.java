package gighub.worketserver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "passcode")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Passcode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "passcode_id")
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "pin", nullable = false, length = 255)
  private String pin;  // 해시값으로 저장

  @Column(name = "created_at")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
    if (this.updatedAt == null) {
      this.updatedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
