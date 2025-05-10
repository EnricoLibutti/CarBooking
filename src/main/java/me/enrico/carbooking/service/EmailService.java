package me.enrico.carbooking.service;

import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Opzionale: nome visibile del mittente
    private static final String FROM_NAME = "CarBooking";

    private String getUserName(User user) {
        return user.getFirstName() != null ? user.getFirstName() : user.getUsername();
    }

    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel) {
        // 'to' deve essere un indirizzo email valido
        if (to == null || !to.contains("@")) {
            logger.error("Tentativo di invio email a un indirizzo non valido o mancante: {}", to);
            return;
        }
        try {
            Context thymeleafContext = new Context();
            templateModel.put("subject", subject);
            thymeleafContext.setVariables(templateModel);

            String htmlBody = thymeleafTemplateEngine.process("mail/" + templateName, thymeleafContext);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Imposta il mittente con nome visualizzato
            helper.setFrom(fromEmail, FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            logger.info("Email HTML '{}' inviata a: {}", subject, to);
        } catch (MessagingException e) {
            logger.error("Errore durante la creazione o l'invio dell'email HTML a {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'invio dell'email HTML a {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Invia un'email di conferma prenotazione.
     */
    public void sendBookingConfirmationEmail(Booking booking) {
        User user = booking.getUser();
        if (user == null || user.getEmail() == null) {
            logger.error("Impossibile inviare email di conferma: utente o email utente non specificati per la prenotazione ID {}", booking.getId());
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("userName", getUserName(user));
        model.put("bookingId", booking.getId());
        model.put("carName", booking.getCar().getName());
        model.put("startTime", booking.getStartDateTime());
        model.put("endTime", booking.getEndDateTime());
        model.put("reason", booking.getReason());

        sendHtmlEmail(user.getEmail(),
                "Conferma Prenotazione Auto - ID: " + booking.getId(),
                "booking-confirmation.html",
                model);
    }

    /**
     * Invia un'email di notifica cancellazione/terminazione.
     */
    public void sendBookingStatusChangeEmail(Booking booking, String statusMessage, String reasonForChange) {
        User user = booking.getUser();
        if (user == null || user.getEmail() == null) {
            logger.error("Impossibile inviare email di modifica stato: utente o email utente non specificati per la prenotazione ID {}", booking.getId());
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("userName", getUserName(user));
        model.put("bookingId", booking.getId());
        model.put("carName", booking.getCar().getName());
        model.put("status", statusMessage);
        model.put("reasonForChange", reasonForChange);

        sendHtmlEmail(user.getEmail(),
                "Notifica Modifica Prenotazione Auto - ID: " + booking.getId(),
                "booking-cancellation.html",
                model);
    }

    /**
     * Invia un'email di promemoria per una prenotazione imminente.
     */
    public void sendBookingReminderEmail(Booking booking) {
        User user = booking.getUser();
        if (user == null || user.getEmail() == null) {
            logger.error("Impossibile inviare email di promemoria: utente o email utente non specificati per la prenotazione ID {}", booking.getId());
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("userName", getUserName(user));
        model.put("bookingId", booking.getId());
        model.put("carName", booking.getCar().getName());
        model.put("startTime", booking.getStartDateTime());
        model.put("endTime", booking.getEndDateTime());

        sendHtmlEmail(user.getEmail(),
                "Promemoria Prenotazione Auto - ID: " + booking.getId(),
                "booking-reminder.html",
                model);
    }

    /**
     * Invia un'email per la reimpostazione della password.
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (user == null || user.getEmail() == null) {
            logger.error("Impossibile inviare email di reset password: utente o email utente non specificati.");
            return;
        }

        String baseUrl = "https://carbooking.libuttienrico.it"; // TODO: Rendere configurabile
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;

        Map<String, Object> model = new HashMap<>();
        model.put("userName", getUserName(user));
        model.put("resetToken", resetToken);
        model.put("resetUrl", resetUrl);

        sendHtmlEmail(user.getEmail(),
                "Richiesta di Reset Password",
                "password-reset.html",
                model);
    }

    /**
     * Invia un'email di test per verificare la configurazione
     */
    public void sendTestEmail(String to, String name) {
        Map<String, Object> model = new HashMap<>();
        model.put("userName", name != null ? name : "Utente");

        sendHtmlEmail(to,
                "CarBooking - Email di Test",
                "test-email.html",
                model);
    }
}