package me.enrico.carbooking;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarbookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarbookingApplication.class, args);
	}

}
