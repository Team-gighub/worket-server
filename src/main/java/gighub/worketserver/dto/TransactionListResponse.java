package gighub.worketserver.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionListResponse {
    private String freelancerName;
    private String totalAmount;
    private List<StatusCountDto> statusCounts;
    private List<TransactionSummaryDto> contractList;
}
