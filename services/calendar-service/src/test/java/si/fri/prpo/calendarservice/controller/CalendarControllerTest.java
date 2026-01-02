package si.fri.prpo.calendarservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import si.fri.prpo.calendarservice.dto.EventResponse;
import si.fri.prpo.calendarservice.service.CalendarEventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@Import(CalendarControllerTest.MockConfig.class)
class CalendarControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private CalendarEventService calendarEventService;

        @TestConfiguration
        static class MockConfig {
                @Bean
                CalendarEventService calendarEventService() {
                        return Mockito.mock(CalendarEventService.class);
                }
        }

    @Test
    void getAllEvents_returnsSeededEvents() throws Exception {
        EventResponse event1 = EventResponse.builder()
                .id(1L)
                .bookingId(1L)
                .userId(2L)
                .facilityId(1L)
                .title("Team Offsite")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        EventResponse event2 = EventResponse.builder()
                .id(2L)
                .bookingId(2L)
                .userId(3L)
                .facilityId(2L)
                .title("Client Workshop")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(3))
                .build();

        when(calendarEventService.getAllEvents()).thenReturn(List.of(event1, event2));

        mockMvc.perform(get("/api/calendar").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[0].title").value("Team Offsite"))
                .andExpect(jsonPath("$[1].title").value("Client Workshop"));
    }
}
