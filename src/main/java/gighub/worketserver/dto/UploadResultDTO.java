package gighub.worketserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UploadResultDTO {
  private final String uploadedContractFileUrl;
  private final String hash;
}
