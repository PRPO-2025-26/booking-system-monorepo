package si.fri.prpo.calendarservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.fri.prpo.calendarservice.entity.CalendarEvent;
import si.fri.prpo.calendarservice.entity.EventStatus;
import si.fri.prpo.calendarservice.entity.SyncStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    // Find by booking
    Optional<CalendarEvent> findByBookingId(Long bookingId);

    List<CalendarEvent> findByUserId(Long userId);

    List<CalendarEvent> findByFacilityId(Long facilityId);

    // Find by Google Calendar ID
    Optional<CalendarEvent> findByGoogleEventId(String googleEventId);

    // Find by status
    List<CalendarEvent> findByStatus(EventStatus status);

    List<CalendarEvent> findBySyncStatus(SyncStatus syncStatus);

    // Find by date range
    List<CalendarEvent> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CalendarEvent> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // Find upcoming events
    List<CalendarEvent> findByUserIdAndStartTimeAfter(Long userId, LocalDateTime after);

    // Check if booking exists
    boolean existsByBookingId(Long bookingId);

    // Find events that need syncing
    List<CalendarEvent> findBySyncStatusAndStatusNot(SyncStatus syncStatus, EventStatus status);
}
