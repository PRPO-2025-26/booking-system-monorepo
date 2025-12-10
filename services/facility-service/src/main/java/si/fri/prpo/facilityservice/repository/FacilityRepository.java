package si.fri.prpo.facilityservice.repository;

import si.fri.prpo.facilityservice.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findByType(String type);

    List<Facility> findByOwnerId(Long ownerId);

    List<Facility> findByAvailable(Boolean available);

    List<Facility> findByNameContainingIgnoreCase(String name);
}
