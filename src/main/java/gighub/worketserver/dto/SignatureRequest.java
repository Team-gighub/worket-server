package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureRequest {
    private String signatureUrl;
}
