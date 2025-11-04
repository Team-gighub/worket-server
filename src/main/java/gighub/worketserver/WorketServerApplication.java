package gighub.worketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorketServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorketServerApplication.class,args);
    System.out.println("한글 인코딩 테스트");
  }
}
