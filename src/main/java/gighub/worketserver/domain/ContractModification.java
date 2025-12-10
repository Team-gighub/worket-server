package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.ModifyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_modification_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContractModification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "modification_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "status")
  private ModifyStatus status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "content")
  private String content;

  public void updateStatus(ModifyStatus status) {
    this.status = status;
  }
}
