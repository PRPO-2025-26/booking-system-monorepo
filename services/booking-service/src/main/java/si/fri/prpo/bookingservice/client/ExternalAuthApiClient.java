package si.fri.prpo.bookingservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalAuthApiClient {

    private final @Qualifier("externalAuthWebClient") WebClient externalAuthWebClient;

    @Value("${external.api.token:}")
    private String externalApiToken;

    public Map<String, Object> getAuthStatus() {
        try {
            return externalAuthWebClient
                    .get()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + externalApiToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("External API error: {}", ex.getMessage());
                        return Mono.error(ex);
                    })
                    .block();
        } catch (Exception e) {
            log.error("Failed to call external API", e);
            throw new IllegalStateException("External API unavailable", e);
        }
    }
}
