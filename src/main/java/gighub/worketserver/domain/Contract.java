package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.ContractType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Contract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contract_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "freelancer_id", nullable = false)
  private User freelancer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  private User client;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 20)
  @Builder.Default
  private ContractType type = ContractType.CREATED;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "client_name", length = 50)
  private String clientName;

  @Column(name = "client_phone", length = 20)
  private String clientPhone;

  @Column(name = "freelancer_sign", length = 255)
  private String freelancerSign;

  @Column(name = "client_sign", length = 255)
  private String clientSign;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "created_at")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  public void updateFreelancerSignUrl(String signUrl) {
    this.freelancerSign = signUrl;
  }

  public void updateClientSignUrl(String signUrl) {
    this.clientSign = signUrl;
  }
}
