package nl.hu.ikr.notification.rest;

import nl.hu.ikr.notification.application.NotificationService;
import nl.hu.ikr.notification.domain.Notification;

import nl.hu.ikr.notification.domain.NotificationType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public List<Notification> getNotifications(
            @PathVariable UUID userId
    ) {
        return service.getNotifications(userId);
    }

    @PostMapping("/test")
    public Notification createTestNotification() {

        return service.createNotification(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Registration Confirmed",
                "You have successfully registered.",
                NotificationType.REGISTRATION_CONFIRMED
        );
    }
}