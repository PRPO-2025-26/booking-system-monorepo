package si.fri.prpo.calendarservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long facilityId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 500)
    private String location;

    // Google Calendar specific fields
    @Column(length = 255, unique = true)
    private String googleEventId;

    @Column(length = 500)
    private String googleCalendarId;

    @Column(length = 1000)
    private String googleEventLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Column
    private LocalDateTime lastSyncedAt;

    @Column(length = 500)
    private String syncErrorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = EventStatus.SCHEDULED;
        }
        if (syncStatus == null) {
            syncStatus = SyncStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
