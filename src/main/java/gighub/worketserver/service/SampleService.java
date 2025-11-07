package gighub.worketserver.service;

import gighub.worketserver.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SampleService {
  private final SampleRepository sampleRepository;

  public String sayHello() {
    return "Hello, Worket!";
  }
}

