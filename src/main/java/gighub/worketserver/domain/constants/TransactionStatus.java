package gighub.worketserver.domain.constants;

public enum TransactionStatus {
    CREATED,            // 생성됨
    SIGNED,             // 서명 완료
    DEPOSIT_HOLD,       // 입금 보류
    PAYMENT_CONFIRMED,  // 결제 확인
    SETTLED             // 정산 완료
}
