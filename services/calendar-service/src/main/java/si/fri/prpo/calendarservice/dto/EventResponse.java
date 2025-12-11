package si.fri.prpo.calendarservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.fri.prpo.calendarservice.entity.EventStatus;
import si.fri.prpo.calendarservice.entity.SyncStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

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
    private EventStatus status;
    private SyncStatus syncStatus;
    private LocalDateTime lastSyncedAt;
    private String syncErrorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
