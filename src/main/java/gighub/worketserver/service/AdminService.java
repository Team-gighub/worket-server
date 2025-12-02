package gighub.worketserver.service;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.ContractModify;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.ModifyStatus;
import gighub.worketserver.dto.*;
import gighub.worketserver.repository.ContractModifyRepository;
import gighub.worketserver.repository.ContractRepository;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static gighub.worketserver.service.TransactionService.DATE_FORMATTER;

/**
 * 관리자 페이지 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
  private final TransactionRepository transactionRepository;
  private final ContractModifyRepository contractModifyRepository;
  private final ContractRepository contractRepository;

  /**
   * 수정 계약서 요청
   */
  public void createModification(ContractModifyRequest request) {
    Transaction transaction = transactionRepository.findById(request.getTransactionId()).
      orElseThrow(() -> new RuntimeException("Transaction not found"));
    ContractModify contractModify = ContractModify.builder()
      .transaction(transaction)
      .userName(request.getUserName())
      .status(ModifyStatus.PENDING)
      .createdAt(LocalDateTime.now())
      .content(request.getContent())
      .build();
    contractModifyRepository.save(contractModify);
  }

  /**
   * 수정 계약서 조회
   */
  public List<ContractModificationResponse> getModificationList() {
    List<ContractModify> contractModifyList = contractModifyRepository.findAll();
    // 2. Stream을 사용하여 리스트를 즉시 변환합니다.
    List<ContractModificationResponse> responseList = contractModifyList.stream()
      .map(entity -> ContractModificationResponse.builder()
        // Long 타입 ID를 그대로 사용
        .modificationId(entity.getId())

        // Transaction 엔티티에서 ID를 가져옵니다. (Lazy Loading 주의 필요)
        .transactionId(entity.getTransaction().getId())

        .userName(entity.getUserName())
        // ENUM을 문자열로 변환 (.name() 사용)
        .status(entity.getStatus().name())

        // LocalDateTime 사용
        .createdAt(entity.getCreatedAt())
        .build())
      .collect(Collectors.toList());
    return responseList;
  }

  /**
   * 수정 계약서 단건 조회
   */
  public ContractModificationDetail getModificationById(Long id) {
    ContractModify contractModify = contractModifyRepository.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
    Transaction transaction = transactionRepository.findById(contractModify.getTransaction().getId()).orElseThrow(() -> new RuntimeException("Transaction not found"));
    Contract contract = contractRepository.findById(transaction.getContract().getId()).orElseThrow(() -> new RuntimeException("Contract not found"));
    ContractModificationDetail detail = new ContractModificationDetail();
    //사용자 정보
    ClientInfoDto clientInfoDto = ClientInfoDto.builder()
      .name(contract.getClient().getName())
      .phone(contract.getClient().getPhone())
      .build();

    //프리랜서 정보
    FreelancerInfoDto freelancerInfoDto = FreelancerInfoDto.builder()
      .name(contract.getFreelancer().getName())
      .phone(contract.getFreelancer().getPhone())
      .account(transaction.getFreelancerAccount())
      .bank(transaction.getFreelancerBank())
      .build();

    //계약정보
    ContractInfoDto contractInfoDto = ContractInfoDto.builder()
      .title(contract.getTitle())
      .amount(contract.getAmount())
      .startDate(contract.getStartDate() != null ? contract.getStartDate().format(DATE_FORMATTER) : null)
      .endDate(contract.getEndDate() != null ? contract.getEndDate().format(DATE_FORMATTER) : null)
      .build();

    detail.setClientInfoDto(clientInfoDto);
    detail.setFreelancerInfoDto(freelancerInfoDto);
    detail.setContractInfoDto(contractInfoDto);
    detail.setContent(contractModify.getContent());
    return detail;
  }

  /**
   * 수정 계약서 변경 적용
   */
  public void applyModification(Long id, ContractModificationDetail detail) {
    ContractModify modify = contractModifyRepository.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
    Transaction transaction = transactionRepository.findById(modify.getTransaction().getId()).orElseThrow(() -> new RuntimeException("Transaction not found"));
    Contract contract = contractRepository.findById(transaction.getContract().getId()).orElseThrow(() -> new RuntimeException("Contract not found"));
    //프리랜서 정보 변경
    transaction.updateFreelancerInfo(detail.getFreelancerInfoDto());
    // 클라이언트 정보 변경
    contract.updateClientInfo(detail.getClientInfoDto());
    // 계약 정보 변경
    contract.updateContractInfo(detail.getContractInfoDto());

    contractRepository.save(contract);
    transactionRepository.save(transaction);
    // 싱테 변경
    modify.updateStatus(ModifyStatus.APPROVED);
    contractModifyRepository.save(modify);

  }
}
