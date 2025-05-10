package me.enrico.carbooking.service;

import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.repositories.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");
    private static final int REMINDER_WINDOW_HOURS = 24; // Invia promemoria per prenotazioni entro le prossime 24 ore

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Scheduled(cron = "${reminder.service.cron}")
    @Transactional
    public void sendUpcomingBookingReminders() {
        LocalDateTime now = LocalDateTime.now(ROME_ZONE);
        LocalDateTime reminderWindowEnd = now.plusHours(REMINDER_WINDOW_HOURS);

        logger.info("Esecuzione del task di invio promemoria prenotazioni. Ricerca prenotazioni tra {} e {}", now, reminderWindowEnd);

        List<Booking> upcomingBookings;
        try {
            upcomingBookings = bookingRepository.findByActiveTrueAndReminderSentFalseAndStartDateTimeBetween(now, reminderWindowEnd);
        } catch (Exception e) {
            logger.error("Errore durante il recupero delle prenotazioni per l'invio dei promemoria.", e);
            return;
        }

        if (upcomingBookings.isEmpty()) {
            logger.info("Nessuna prenotazione imminente trovata per cui inviare un promemoria.");
            return;
        }

        logger.info("Trovate {} prenotazioni per cui inviare un promemoria.", upcomingBookings.size());

        for (Booking booking : upcomingBookings) {
            try {
                logger.info("Invio promemoria per la prenotazione ID: {}", booking.getId());
                emailService.sendBookingReminderEmail(booking);
                booking.setReminderSent(true);
                bookingRepository.save(booking);
                logger.info("Promemoria inviato e stato aggiornato per la prenotazione ID: {}", booking.getId());
            } catch (Exception e) {
                logger.error("Errore durante l'invio del promemoria per la prenotazione ID {} o durante l'aggiornamento dello stato: {}", booking.getId(), e.getMessage(), e);
            }
        }
        logger.info("Task di invio promemoria prenotazioni completato.");
    }
}