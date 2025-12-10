package si.fri.prpo.facilityservice.service;

import si.fri.prpo.facilityservice.dto.FacilityRequest;
import si.fri.prpo.facilityservice.dto.FacilityResponse;
import si.fri.prpo.facilityservice.entity.Facility;
import si.fri.prpo.facilityservice.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;

    // Ustvari nov objekt (samo ADMIN ali FACILITY_MANAGER)
    @Transactional
    public FacilityResponse createFacility(FacilityRequest request, Long ownerId) {
        Facility facility = new Facility();
        facility.setName(request.getName());
        facility.setType(request.getType());
        facility.setAddress(request.getAddress());
        facility.setDescription(request.getDescription());
        facility.setCapacity(request.getCapacity());
        facility.setPricePerHour(request.getPricePerHour());
        facility.setOwnerId(ownerId);
        facility.setAvailable(request.getAvailable());

        Facility saved = facilityRepository.save(facility);
        return mapToResponse(saved);
    }

    // Pridobi vse objekte
    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Pridobi objekt po ID
    public FacilityResponse getFacilityById(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found with id: " + id));
        return mapToResponse(facility);
    }

    // Pridobi objekte po tipu
    public List<FacilityResponse> getFacilitiesByType(String type) {
        return facilityRepository.findByType(type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Pridobi objekte po lastniku
    public List<FacilityResponse> getFacilitiesByOwner(Long ownerId) {
        return facilityRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Pridobi dostopne objekte
    public List<FacilityResponse> getAvailableFacilities() {
        return facilityRepository.findByAvailable(true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Posodobi objekt
    @Transactional
    public FacilityResponse updateFacility(Long id, FacilityRequest request, Long userId) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found with id: " + id));

        // Preveri da je uporabnik lastnik (v produkciji bi preveril tudi ADMIN vlogo)
        if (!facility.getOwnerId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this facility");
        }

        facility.setName(request.getName());
        facility.setType(request.getType());
        facility.setAddress(request.getAddress());
        facility.setDescription(request.getDescription());
        facility.setCapacity(request.getCapacity());
        facility.setPricePerHour(request.getPricePerHour());
        facility.setAvailable(request.getAvailable());

        Facility updated = facilityRepository.save(facility);
        return mapToResponse(updated);
    }

    // Pobriši objekt
    @Transactional
    public void deleteFacility(Long id, Long userId) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility not found with id: " + id));

        // Preveri da je uporabnik lastnik
        if (!facility.getOwnerId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this facility");
        }

        facilityRepository.delete(facility);
    }

    // Mapper helper
    private FacilityResponse mapToResponse(Facility facility) {
        return new FacilityResponse(
                facility.getId(),
                facility.getName(),
                facility.getType(),
                facility.getAddress(),
                facility.getDescription(),
                facility.getCapacity(),
                facility.getPricePerHour(),
                facility.getOwnerId(),
                facility.getAvailable(),
                facility.getCreatedAt(),
                facility.getUpdatedAt());
    }
}
