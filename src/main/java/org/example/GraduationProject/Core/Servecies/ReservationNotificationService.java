package org.example.GraduationProject.Core.Servecies;

import org.example.GraduationProject.Common.Entities.Reservations;
import org.example.GraduationProject.Core.Repsitories.reservationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationNotificationService {

    @Autowired
    private reservationsRepository reservationsRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 23 16 * * *") // Runs daily at 3:57 PM
    public void sendReservationNotifications() {
        // Get tomorrow's start and end time
        LocalDateTime startOfDay = LocalDateTime.now().plusDays(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().plusDays(1)
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Fetch reservations within tomorrow's range
        List<Reservations> reservations = reservationsRepository.findReservationsForTomorrow(startOfDay, endOfDay);

        System.out.println("Found " + reservations.size() + " reservations for tomorrow " + startOfDay);

        for (Reservations reservation : reservations) {
            try {
                // Get customer email
                String email = reservation.getCustomer().getUser().getEmail();

                // Email content
                String subject = "Reminder: Upcoming Reservation";
                String message = createReservationReminderMessage(reservation);

                // Send email
                emailService.sentNotificationEmail(email, subject, message);

                // Mark the reservation as notified
                reservation.setNotificationSent(true);
                reservationsRepository.save(reservation);

            } catch (Exception e) {
                // Log the error to prevent scheduler failure
                e.printStackTrace();
            }
        }
    }


    private String createReservationReminderMessage(Reservations reservation) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Reservation Reminder</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }" +
                "        .email-container { max-width: 600px; margin: 20px auto; background-color: #fff; " +
                "                          border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }" +
                "        .header { background-color: #007bff; color: #fff; padding: 20px; text-align: center; }" +
                "        .content { padding: 20px; line-height: 1.6; }" +
                "        .content p { margin: 10px 0; }" +
                "        .content strong { color: #333; }" +
                "        .footer { background-color: #f4f4f4; color: #777; text-align: center; padding: 10px; font-size: 12px; }" +
                "        .btn { display: inline-block; background-color: #007bff; color: #fff; padding: 10px 20px; " +
                "               border-radius: 5px; text-decoration: none; margin-top: 20px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"email-container\">" +
                "        <!-- Header -->" +
                "        <div class=\"header\">" +
                "            <h1>Reservation Reminder</h1>" +
                "        </div>" +

                "        <!-- Content -->" +
                "        <div class=\"content\">" +
                "            <p>Dear <strong>" + reservation.getCustomer().getUser().getFirstName() + " " + reservation.getCustomer().getUser().getLastName() + "</strong>,</p> "+
                "            <p>This is a friendly reminder for your upcoming reservation:</p>" +
                "            <p><strong>Hall Name:</strong> " + reservation.getHall().getName() + "</p>" +
                "            <p><strong>Reservation Date:</strong> " + reservation.getDate() + "</p>" +
                "            <p><strong>Location:</strong> " + reservation.getHall().getLocation() + "</p>" +
                "            <p>We look forward to serving you!</p>" +
                "        </div>" +

                "        <!-- Footer -->" +
                "        <div class=\"footer\">" +
                "            <p>&copy; 2024 4everbooking. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

}
