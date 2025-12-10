package si.fri.prpo.facilityservice.controller;

import si.fri.prpo.facilityservice.dto.FacilityRequest;
import si.fri.prpo.facilityservice.dto.FacilityResponse;
import si.fri.prpo.facilityservice.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    // CREATE - Ustvari nov objekt
    @PostMapping
    public ResponseEntity<FacilityResponse> createFacility(
            @Valid @RequestBody FacilityRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        // V produkciji bi userId pridobil iz JWT tokena
        FacilityResponse response = facilityService.createFacility(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ - Pridobi vse objekte
    @GetMapping
    public ResponseEntity<List<FacilityResponse>> getAllFacilities() {
        List<FacilityResponse> facilities = facilityService.getAllFacilities();
        return ResponseEntity.ok(facilities);
    }

    // READ - Pridobi objekt po ID
    @GetMapping("/{id}")
    public ResponseEntity<FacilityResponse> getFacilityById(@PathVariable Long id) {
        FacilityResponse facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    // READ - Pridobi objekte po tipu
    @GetMapping("/type/{type}")
    public ResponseEntity<List<FacilityResponse>> getFacilitiesByType(@PathVariable String type) {
        List<FacilityResponse> facilities = facilityService.getFacilitiesByType(type);
        return ResponseEntity.ok(facilities);
    }

    // READ - Pridobi objekte po lastniku
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<FacilityResponse>> getFacilitiesByOwner(@PathVariable Long ownerId) {
        List<FacilityResponse> facilities = facilityService.getFacilitiesByOwner(ownerId);
        return ResponseEntity.ok(facilities);
    }

    // READ - Pridobi dostopne objekte
    @GetMapping("/available")
    public ResponseEntity<List<FacilityResponse>> getAvailableFacilities() {
        List<FacilityResponse> facilities = facilityService.getAvailableFacilities();
        return ResponseEntity.ok(facilities);
    }

    // UPDATE - Posodobi objekt
    @PutMapping("/{id}")
    public ResponseEntity<FacilityResponse> updateFacility(
            @PathVariable Long id,
            @Valid @RequestBody FacilityRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        FacilityResponse response = facilityService.updateFacility(id, request, userId);
        return ResponseEntity.ok(response);
    }

    // DELETE - Pobriši objekt
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        facilityService.deleteFacility(id, userId);
        return ResponseEntity.noContent().build();
    }
}
