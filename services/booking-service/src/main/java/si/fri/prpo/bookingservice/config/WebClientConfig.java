package si.fri.prpo.bookingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    @Value("${services.calendar.url}")
    private String calendarServiceUrl;

    @Value("${services.notification.url}")
    private String notificationServiceUrl;

    @Bean("paymentWebClient")
    public WebClient paymentWebClient() {
        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    @Bean("calendarWebClient")
    public WebClient calendarWebClient() {
        return WebClient.builder()
                .baseUrl(calendarServiceUrl)
                .build();
    }

    @Bean("notificationWebClient")
    public WebClient notificationWebClient() {
        return WebClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}
