package gighub.worketserver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContractFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "file_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id", nullable = false)
  private Contract contract;

  @Column(name = "file_url", nullable = false, length = 255)
  private String fileUrl;

  @Column(name = "file_hash", nullable = false, length = 64)
  private String fileHash;

  @Column(name = "uploaded_at")
  @Builder.Default
  private LocalDateTime uploadedAt = LocalDateTime.now();

  @PrePersist
  public void prePersist() {
    if (this.uploadedAt == null) {
      this.uploadedAt = LocalDateTime.now();
    }
  }
}
