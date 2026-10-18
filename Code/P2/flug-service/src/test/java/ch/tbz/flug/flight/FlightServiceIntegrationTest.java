package ch.tbz.flug.flight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import ch.tbz.flug.airport.AirportClient;
import ch.tbz.flug.airport.AirportClientProperties;

/**
 * Prüft die Kommunikation vom Flugservice zum Flughafenservice.
 */
class FlightServiceIntegrationTest {

    /**
     * Ein gültiger Flug wird erstellt, nachdem der Flughafenservice beide Flughäfen liefert.
     */
    @Test
    void createsFlightAfterReadingAirportsFromAirportService() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer airportServer = MockRestServiceServer.bindTo(builder).build();
        AirportClient airportClient = new AirportClient(builder,
                new AirportClientProperties("http://airport-service"));
        FlightRepository repository = mock(FlightRepository.class);
        when(repository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FlightService service = new FlightService(repository, airportClient,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

        airportServer.expect(requestTo("http://airport-service/api/airports"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        [
                          {"id":"1","name":"Zürich","code":"ZRH","capacity":1000},
                          {"id":"2","name":"New York","code":"JFK","capacity":2000}
                        ]
                        """, APPLICATION_JSON));

        Flight result = service.create(new FlightCreateRequest(
                "ZRH", "JFK",
                Instant.parse("2026-10-01T08:00:00Z"),
                Instant.parse("2026-10-01T16:00:00Z"),
                "Boeing 727"));

        airportServer.verify();
        assertThat(result.getDepartureAirportCode()).isEqualTo("ZRH");
        assertThat(result.getArrivalAirportCode()).isEqualTo("JFK");
    }

    /**
     * Ein unbekannter Flughafen wird abgelehnt, wenn der Flughafenservice ihn nicht liefert.
     */
    @Test
    void rejectsFlightWhenAirportServiceDoesNotKnowAirport() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer airportServer = MockRestServiceServer.bindTo(builder).build();
        AirportClient airportClient = new AirportClient(builder,
                new AirportClientProperties("http://airport-service"));
        FlightRepository repository = mock(FlightRepository.class);
        FlightService service = new FlightService(repository, airportClient);

        airportServer.expect(requestTo("http://airport-service/api/airports"))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", APPLICATION_JSON));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(new FlightCreateRequest(
                "ZRH", "JFK",
                Instant.parse("2026-10-01T08:00:00Z"),
                Instant.parse("2026-10-01T16:00:00Z"),
                "Boeing 727")))
                .isInstanceOf(UnknownAirportException.class);

        airportServer.verify();
    }
}
