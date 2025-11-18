package gighub.worketserver.domain.constants;

public enum TransactionStatus {
  CREATED,            // 생성됨
  SIGNED,             // 서명 완료
  DEPOSIT_HOLD,       // 예치 완료
  PAYMENT_CONFIRMED,  // 지급 확정
  SETTLED             // 정산 완료
}
