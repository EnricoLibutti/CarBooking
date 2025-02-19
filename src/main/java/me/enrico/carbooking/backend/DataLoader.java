package me.enrico.carbooking.backend;

import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.repositories.CarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(CarRepository carRepository) {
        return args -> {
            if (carRepository.count() == 0) {
                carRepository.save(new Car("Mini Nera", 4 , true));
                carRepository.save(new Car("Mine Bianca", 4, true));
                carRepository.save(new Car("Opel Mokka", 5, true));
            }
        };
    }
}
