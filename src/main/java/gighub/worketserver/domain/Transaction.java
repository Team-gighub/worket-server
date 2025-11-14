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
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id")
    private User freelancer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.CREATED;

    @Column(name = "settled_amount", precision = 15, scale = 2)
    private BigDecimal settledAmount;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "deposit_hold_at")
    private LocalDateTime depositHoldAt;

    @Column(name = "payment_confirmed_at")
    private LocalDateTime paymentConfirmedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

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
