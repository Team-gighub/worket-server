package gighub.worketserver.controller;

import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.ContractUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/contracts")
@RestController
@RequiredArgsConstructor
public class ContractController {

  private final ContractUploadService contractService;

  @PostMapping("/extract")
  public ApiResponse<?> extract(
    @RequestPart("file") MultipartFile file,
    @RequestPart("message") String message
  ) {
    return contractService.process(file, message);
  }
}
