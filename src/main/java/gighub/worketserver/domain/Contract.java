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
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ContractType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Client Info
    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(name = "client_phone", nullable = false, length = 20)
    private String clientPhone;

    // Freelancer Info
    @Column(name = "freelancer_name", nullable = false, length = 100)
    private String freelancerName;

    @Column(name = "freelancer_phone", nullable = false, length = 20)
    private String freelancerPhone;

    @Column(name = "freelancer_account", length = 50)
    private String freelancerAccount;

    @Column(name = "freelancer_bank", length = 50)
    private String freelancerBank;

    // Signatures
    @Column(name = "freelancer_signature_url", length = 500)
    private String freelancerSignatureUrl;

    @Column(name = "client_signature_url", length = 500)
    private String clientSignatureUrl;

    @Column(name = "contract_file_url", length = 500)
    private String contractFileUrl;

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
