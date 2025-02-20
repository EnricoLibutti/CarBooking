/*
package me.enrico.carbooking.task;


import me.enrico.carbooking.repositories.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    @Autowired
    private CarRepository carRepository;

    @Scheduled(fixedRate = 60000) // Esegui ogni minuto
    public void updateCarAvailability() {
        LocalDateTime now = LocalDateTime.now();
        carRepository.findAll().forEach(car -> {
            if (!car.isAvailable() && (car.getAvailableUntil() == null || car.getAvailableUntil().isBefore(now))) {
                car.setAvailable(true);
                car.setAvailableUntil(null);
                carRepository.save(car);
            }
        });
    }
}
*/
