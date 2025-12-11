package si.fri.prpo.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {
    private Long id;
    private Long bookingId;
    private Long userId;
    private Long facilityId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String googleEventId;
    private String googleCalendarId;
    private String googleEventLink;
    private String status;
    private String syncStatus;
    private LocalDateTime createdAt;
}
