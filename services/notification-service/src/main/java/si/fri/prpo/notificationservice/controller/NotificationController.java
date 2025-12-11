package si.fri.prpo.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.fri.prpo.notificationservice.dto.NotificationRequest;
import si.fri.prpo.notificationservice.dto.NotificationResponse;
import si.fri.prpo.notificationservice.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service",
                "timestamp", java.time.LocalDateTime.now()));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Received notification request: {}", request);
        NotificationResponse response = notificationService.sendNotification(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByBookingId(@PathVariable Long bookingId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByBookingId(bookingId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByPaymentId(@PathVariable Long paymentId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByPaymentId(paymentId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByEventId(@PathVariable Long eventId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByEventId(eventId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationResponse> retryNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.retryNotification(id);
        return ResponseEntity.ok(response);
    }
}
