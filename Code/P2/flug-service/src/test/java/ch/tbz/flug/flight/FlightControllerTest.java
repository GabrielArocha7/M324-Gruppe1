package ch.tbz.flug.flight;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService service;

    /** Prüft, dass ein gültiger Flug mit HTTP 201 erstellt wird. */
    @Test
    void createsFlightForValidRequest() throws Exception {
        when(service.create(any())).thenReturn(flight());

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated());
    }

    /** Prüft, dass ein fehlender Flugzeugtyp mit HTTP 400 abgelehnt wird. */
    @Test
    void rejectsMissingAircraftType() throws Exception {
        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departureAirportCode\":\"ZRH\",\"arrivalAirportCode\":\"JFK\",\"departureAt\":\"2026-10-01T08:00:00Z\",\"arrivalAt\":\"2026-10-01T16:00:00Z\"}"))
                .andExpect(status().isBadRequest());
    }

    /** Prüft, dass gleiche Start- und Zielflughäfen mit HTTP 400 abgelehnt werden. */
    @Test
    void rejectsSameAirports() throws Exception {
        when(service.create(any())).thenThrow(new SameAirportException());

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadRequest());
    }

    /** Prüft, dass ein unbekannter Flughafen mit HTTP 404 abgelehnt wird. */
    @Test
    void rejectsUnknownAirport() throws Exception {
        when(service.create(any())).thenThrow(new UnknownAirportException("JFK"));

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isNotFound());
    }

    private String validJson() {
        return "{\"departureAirportCode\":\"ZRH\",\"arrivalAirportCode\":\"JFK\","
                + "\"departureAt\":\"2026-10-01T08:00:00Z\","
                + "\"arrivalAt\":\"2026-10-01T16:00:00Z\",\"aircraftType\":\"Boeing 727\"}";
    }

    private Flight flight() {
        return new Flight("id-1", "ZRH", "JFK",
                Instant.parse("2026-10-01T08:00:00Z"),
                Instant.parse("2026-10-01T16:00:00Z"),
                "Boeing 727", Instant.parse("2026-01-01T00:00:00Z"));
    }
}
