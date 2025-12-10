package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.ContractType;
import gighub.worketserver.dto.ClientInfoDto;
import gighub.worketserver.dto.ContractInfoDto;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.CustomException;
import gighub.worketserver.global.exception.ErrorCode;
import gighub.worketserver.global.exception.RestApiException;
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

  public void updateClient(User user) {
    this.client = user;
  }

  /**
   * 클라이언트 정보 변경 - 관리자용
   */
  public void updateClientInfo(ClientInfoDto clientInfoDto) {
    this.clientName = clientInfoDto.getName();
    this.clientPhone = clientInfoDto.getPhone();
  }

  /**
   * 거래 정보 변경 - 관리자용
   */
  public void updateContractInfo(ContractInfoDto contractInfoDto) {
    try {
      if (contractInfoDto.getStartDate() != null) {
        this.startDate = LocalDate.parse(contractInfoDto.getStartDate());
      }
      if (contractInfoDto.getEndDate() != null) {
        this.endDate = LocalDate.parse(contractInfoDto.getEndDate());
      }
      this.title = contractInfoDto.getTitle();
    } catch (java.time.format.DateTimeParseException e) {
      throw new RestApiException(CommonErrorCode.BAD_REQUEST, "날짜 형식이 올바르지 않습니다.");
    }
  }
}
