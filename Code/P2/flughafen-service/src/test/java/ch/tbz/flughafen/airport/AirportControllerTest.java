package ch.tbz.flughafen.airport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AirportController.class)
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AirportService service;

    /** Prüft, dass ein gültiger Flughafen mit HTTP 201 erstellt wird. */
    @Test
    void createsAirportForValidRequest() throws Exception {
        Airport airport = airport("ZRH");
        when(service.create(any())).thenReturn(airport);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AirportCreateRequest("Zürich", "zrh", 30000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ZRH"));
    }

    /** Prüft, dass ein ungültiges Flughafenkürzel mit HTTP 400 abgelehnt wird. */
    @Test
    void rejectsInvalidAirportCode() throws Exception {
        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zürich\",\"code\":\"ZH\",\"capacity\":30000}"))
                .andExpect(status().isBadRequest());
    }

    /** Prüft, dass ein bereits verwendetes Kürzel mit HTTP 409 abgelehnt wird. */
    @Test
    void rejectsDuplicateAirportCode() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateAirportCodeException("ZRH"));

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zürich\",\"code\":\"ZRH\",\"capacity\":30000}"))
                .andExpect(status().isConflict());
    }

    /** Prüft, dass vorhandene Flughäfen mit HTTP 200 zurückgegeben werden. */
    @Test
    void returnsAllAirports() throws Exception {
        when(service.findAll()).thenReturn(List.of(airport("ZRH")));

        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Zürich"));
    }

    /** Prüft, dass eine leere Flughafenliste mit HTTP 404 beantwortet wird. */
    @Test
    void returnsNotFoundWhenNoAirportsExist() throws Exception {
        when(service.findAll()).thenThrow(new NoAirportsException());

        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isNotFound());
    }

    private Airport airport(String code) {
        return new Airport("id-1", "Zürich", code, 30000, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
