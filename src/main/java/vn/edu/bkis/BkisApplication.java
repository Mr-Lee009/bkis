package vn.edu.bkis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BkisApplication {

  public static void main(String[] args) {
    SpringApplication.run(BkisApplication.class, args);
  }

}
