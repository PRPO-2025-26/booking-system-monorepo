package si.fri.prpo.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.fri.prpo.notificationservice.dto.NotificationRequest;
import si.fri.prpo.notificationservice.dto.NotificationResponse;
import si.fri.prpo.notificationservice.entity.NotificationChannel;
import si.fri.prpo.notificationservice.entity.NotificationLog;
import si.fri.prpo.notificationservice.entity.NotificationStatus;
import si.fri.prpo.notificationservice.exception.NotificationException;
import si.fri.prpo.notificationservice.exception.NotificationNotFoundException;
import si.fri.prpo.notificationservice.repository.NotificationLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending notification: type={}, channel={}, recipient={}",
                request.getType(), request.getChannel(), request.getRecipient());

        // Create notification log
        NotificationLog notificationLog = NotificationLog.builder()
                .userId(request.getUserId())
                .bookingId(request.getBookingId())
                .paymentId(request.getPaymentId())
                .eventId(request.getEventId())
                .type(request.getType())
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        notificationLog = notificationLogRepository.save(notificationLog);

        // Send notification based on channel
        try {
            if (request.getChannel() == NotificationChannel.EMAIL) {
                sendEmailNotification(request);
            } else {
                throw new NotificationException("Unsupported notification channel: " + request.getChannel());
            }

            // Update status to SENT or MOCK
            notificationLog.setStatus(emailService.isMockMode() ? NotificationStatus.MOCK : NotificationStatus.SENT);
            notificationLog.setSentAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Failed to send notification", e);
            notificationLog.setStatus(NotificationStatus.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            throw new NotificationException("Failed to send notification: " + e.getMessage(), e);
        } finally {
            notificationLog = notificationLogRepository.save(notificationLog);
        }

        return mapToResponse(notificationLog);
    }

    private void sendEmailNotification(NotificationRequest request) {
        emailService.sendSimpleEmail(
                request.getRecipient(),
                request.getSubject(),
                request.getContent());
    }

    public NotificationResponse getNotificationById(Long id) {
        NotificationLog log = notificationLogRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        return mapToResponse(log);
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationLogRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByBookingId(Long bookingId) {
        return notificationLogRepository.findByBookingId(bookingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByPaymentId(Long paymentId) {
        return notificationLogRepository.findByPaymentId(paymentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByEventId(Long eventId) {
        return notificationLogRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse retryNotification(Long id) {
        NotificationLog log = notificationLogRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));

        if (log.getStatus() != NotificationStatus.FAILED) {
            throw new NotificationException("Can only retry failed notifications");
        }

        log.setRetryCount(log.getRetryCount() + 1);
        log.setStatus(NotificationStatus.PENDING);
        log.setErrorMessage(null);

        NotificationRequest retryRequest = NotificationRequest.builder()
                .userId(log.getUserId())
                .bookingId(log.getBookingId())
                .paymentId(log.getPaymentId())
                .eventId(log.getEventId())
                .type(log.getType())
                .channel(log.getChannel())
                .recipient(log.getRecipient())
                .subject(log.getSubject())
                .content(log.getContent())
                .build();

        return sendNotification(retryRequest);
    }

    private NotificationResponse mapToResponse(NotificationLog log) {
        return NotificationResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .bookingId(log.getBookingId())
                .paymentId(log.getPaymentId())
                .eventId(log.getEventId())
                .type(log.getType())
                .channel(log.getChannel())
                .recipient(log.getRecipient())
                .subject(log.getSubject())
                .content(log.getContent())
                .status(log.getStatus())
                .sentAt(log.getSentAt())
                .errorMessage(log.getErrorMessage())
                .retryCount(log.getRetryCount())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}
