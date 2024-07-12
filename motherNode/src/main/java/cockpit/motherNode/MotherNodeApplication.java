package cockpit.motherNode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MotherNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotherNodeApplication.class, args);
        System.out.println("Hello");
    }

}
