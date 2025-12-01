package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "transaction_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id", nullable = false)
  private Contract contract;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  private TransactionStatus status = TransactionStatus.CREATED;

  @Column(name = "settlement_amount", precision = 15, scale = 2)
  private BigDecimal settlementAmount;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "client_bank", length = 50)
  private String clientBank;

  @Column(name = "client_account", length = 100)
  private String clientAccount;

  @Column(name = "freelancer_bank", length = 50)
  private String freelancerBank;

  @Column(name = "freelancer_account", length = 100)
  private String freelancerAccount;

  @Column(name = "signed_at")
  private LocalDateTime signedAt;

  @Column(name = "deposit_hold_at")
  private LocalDateTime depositHoldAt;

  @Column(name = "payment_confirmed_at")
  private LocalDateTime paymentConfirmedAt;

  @Column(name = "settled_at")
  private LocalDateTime settledAt;

  @Column(name = "settlement_tx_id")
  private Long settlementTxId;

  @Column(name = "created_at")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "escrow_confim_tid")
  private String escrowConfimId;

  @Column(name="settlement_tid")
  private String settlementId;

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

  /**
   * 거래 상태 변경
   */
  public void updateStatus(TransactionStatus newStatus) {
    this.status = newStatus;
    LocalDateTime now = LocalDateTime.now();

    switch (newStatus) {
      case SIGNED -> this.signedAt = now;
      case DEPOSIT_HOLD -> this.depositHoldAt = now;
      case PAYMENT_CONFIRMED -> this.paymentConfirmedAt = now;
      case SETTLED -> this.settledAt = now;
    }
  }
}
