package si.fri.prpo.notificationservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import si.fri.prpo.notificationservice.dto.NotificationResponse;
import si.fri.prpo.notificationservice.entity.NotificationChannel;
import si.fri.prpo.notificationservice.entity.NotificationStatus;
import si.fri.prpo.notificationservice.entity.NotificationType;
import si.fri.prpo.notificationservice.service.NotificationService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTest.MockConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        NotificationService notificationService() {
            return Mockito.mock(NotificationService.class);
        }
    }

    @Test
    void getNotificationById_returnsBody() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(2L)
                .bookingId(1L)
                .paymentId(1L)
                .eventId(1L)
                .type(NotificationType.BOOKING_CONFIRMATION)
                .channel(NotificationChannel.EMAIL)
                .recipient("alice@example.com")
                .subject("Booking confirmed")
                .content("Your booking for Main Hall is confirmed.")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(notificationService.getNotificationById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/notifications/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("BOOKING_CONFIRMATION"))
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.recipient").value("alice@example.com"));
    }
}
