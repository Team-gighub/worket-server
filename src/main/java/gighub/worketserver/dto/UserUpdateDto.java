package gighub.worketserver.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private LocalDate birthDate;
    private String gender;  // String으로 변경 (OpenAPI 명세 준수)
    private String businessSector;
    private Integer businessSectorYears;
    private String businessRegistrationNumber;
}
