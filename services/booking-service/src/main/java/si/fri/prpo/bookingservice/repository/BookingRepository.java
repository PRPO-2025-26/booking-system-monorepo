package si.fri.prpo.bookingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import si.fri.prpo.bookingservice.entity.Booking;
import si.fri.prpo.bookingservice.entity.Booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Najdi vse rezervacije uporabnika
    List<Booking> findByUserId(Long userId);

    // Najdi vse rezervacije za določen objekt
    List<Booking> findByFacilityId(Long facilityId);

    // Najdi vse rezervacije z določenim statusom
    List<Booking> findByStatus(BookingStatus status);

    // Najdi rezervacije uporabnika z določenim statusom
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    // Najdi rezervacije objekta z določenim statusom
    List<Booking> findByFacilityIdAndStatus(Long facilityId, BookingStatus status);

    // Preveri prekrivanje časa za določen objekt (ključna funkcija za preprečevanje
    // dvojnih rezervacij)
    @Query("SELECT b FROM Booking b WHERE b.facilityId = :facilityId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("facilityId") Long facilityId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Najdi prihodnje rezervacije uporabnika
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND b.startTime > :now " +
            "ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // Najdi pretekle rezervacije uporabnika
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND b.endTime < :now " +
            "ORDER BY b.endTime DESC")
    List<Booking> findPastBookingsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // Najdi rezervacije v določenem časovnem obdobju
    @Query("SELECT b FROM Booking b WHERE b.facilityId = :facilityId " +
            "AND b.startTime >= :startDate " +
            "AND b.endTime <= :endDate " +
            "ORDER BY b.startTime ASC")
    List<Booking> findByFacilityIdAndDateRange(
            @Param("facilityId") Long facilityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
