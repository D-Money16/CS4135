package elib.elib_gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import javax.crypto.SecretKey;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

class ApiGatewayIntegrationTest {

    private static final String SECRET = "elib-jwt-key-32bytes!!!!!!!!!!!!!!";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    static WireMockServer identityStub;
    static WireMockServer catalogueStub;
    static WireMockServer lendingStub;

    @LocalServerPort
    int port; 

    @MockitoBean
    LoadBalancerClient loadBalancerClient;

    private RestClient client;

    @BeforeAll
    static void startWireMocks() {

        //mocsks downstreem services

        identityStub  = new WireMockServer(0);
        catalogueStub = new WireMockServer(0);
        lendingStub   = new WireMockServer(0);

        identityStub.start();
        catalogueStub.start();
        lendingStub.start();
    }

    @AfterAll
    static void stopWireMocks() {

        identityStub.stop();
        catalogueStub.stop();
        lendingStub.stop();
    }

    @BeforeEach
    void setUp() {

        identityStub.resetAll();
        catalogueStub.resetAll();
        lendingStub.resetAll();

        // Build instances before stubbing — nesting mock() inside thenReturn() confuses Mockito
        ServiceInstance identityInst  = instance(identityStub);
        ServiceInstance catalogueInst = instance(catalogueStub);
        ServiceInstance lendingInst   = instance(lendingStub);

        when(loadBalancerClient.choose("elib-identity")).thenReturn(identityInst);
        when(loadBalancerClient.choose("elib-catalogue")).thenReturn(catalogueInst);
        when(loadBalancerClient.choose("elib-lending")).thenReturn(lendingInst);

        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private String token(String role) {

        Date now = new Date();

        Date exp = new Date(System.currentTimeMillis() + 3_600_000); //1hr
        return Jwts.builder()
                .setSubject("test-user")
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(KEY)
                .compact();
    }

    private String expiredToken() {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject("test-user")
                .claim("role", "STUDENT")
                .setIssuedAt(new Date(now - 10_000)) // 10s ago
                .setExpiration(new Date(now - 5_000)) //expired 5s ago
                .signWith(KEY)
                .compact();
    }

    private ServiceInstance instance(WireMockServer server) {

        ServiceInstance si = mock(ServiceInstance.class);

        when(si.getUri()).thenReturn(URI.create("http://localhost:" + server.port()));
        return si;
    }

    @Test
    @Order(1)
    @DisplayName("Public path /api/user/auth/** bypasses JWT filter - no token required")

    void publicPath_noTokenRequired() {

        identityStub.stubFor(get(urlPathEqualTo("/api/user/auth/login"))
                .willReturn(okJson("{\"token\":\"preview-token\"}")));

        ResponseEntity<String> resp = client.get()
                .uri("/api/user/auth/login")
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        identityStub.verify(1, getRequestedFor(urlPathEqualTo("/api/user/auth/login")));
    }

    @Test
    @Order(2)
    @DisplayName("Missing Authorization header - 401 'Missing token'")

    void missingToken_returns401() {
        ResponseEntity<String> resp = client.get()
                .uri("/api/lending/loans")
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(resp.getBody()).contains("Missing token");
        lendingStub.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @Order(3)
    @DisplayName("Expired token - 401 'Invalid token'")

    void expiredToken_returns401() {
        ResponseEntity<String> resp = client.get()
                .uri("/api/lending/loans")
                .header("Authorization", "Bearer " + expiredToken())
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(resp.getBody()).contains("Invalid token");
        lendingStub.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @Order(4)
    @DisplayName("Valid token with unrecognized role - 401 'Invalid role'")
    void unknownRole_returns401() {

        ResponseEntity<String> resp = client.get()
                .uri("/api/lending/loans")
                .header("Authorization", "Bearer " + token("LIBRARIAN"))
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(resp.getBody()).contains("Invalid role");
        lendingStub.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @Order(5)
    @DisplayName("STUDENT token - /api/lending/** proxied to Lending service")

    void studentToken_proxiedToLending() {
        lendingStub.stubFor(get(urlPathEqualTo("/api/lending/loans"))
                .willReturn(okJson("[]")));

        ResponseEntity<String> resp = client.get()
                .uri("/api/lending/loans")
                .header("Authorization", "Bearer " + token("STUDENT"))
                .retrieve()
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("[]");
        lendingStub.verify(1, getRequestedFor(urlPathEqualTo("/api/lending/loans")));
    }

    @Test
    @Order(6)
    @DisplayName("STAFF token - /api/book-catalogue/** proxied to Catalogue service")

    void staffToken_proxiedToCatalogue() {
        catalogueStub.stubFor(get(urlPathEqualTo("/api/book-catalogue/books"))
                .willReturn(okJson("[]")));

        ResponseEntity<String> resp = client.get()
                .uri("/api/book-catalogue/books")
                .header("Authorization", "Bearer " + token("STAFF"))
                .retrieve()
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        catalogueStub.verify(1, getRequestedFor(urlPathEqualTo("/api/book-catalogue/books")));
    }

    @Test
    @Order(7)
    @DisplayName("ADMIN token - protected /api/user/** proxied to Identity service")

    void adminToken_proxiedToIdentity() {
        identityStub.stubFor(get(urlPathEqualTo("/api/user/profile"))
                .willReturn(okJson("{\"id\":\"admin-1\"}")));

        ResponseEntity<String> resp = client.get()
                .uri("/api/user/profile")
                .header("Authorization", "Bearer " + token("ADMIN"))
                .retrieve()
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        identityStub.verify(1, getRequestedFor(urlPathEqualTo("/api/user/profile")));
    }

    @Test
    @Order(8)
    @DisplayName("No service instance available")

    void noServiceInstance_returns5xx() {

        when(loadBalancerClient.choose("elib-lending")).thenReturn(null); //outage

        ResponseEntity<String> resp = client.get()
                .uri("/api/lending/loans")
                .header("Authorization", "Bearer " + token("STUDENT"))
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @Order(9)
    @DisplayName("Downstream returns 500 ")

    void downstreamError_propagated() {

        lendingStub.stubFor(get(urlPathEqualTo("/api/lending/loans"))
                .willReturn(serverError().withBody("Downstream has exploded :(")));


        HttpServerErrorException ex = org.junit.jupiter.api.Assertions.assertThrows(
                HttpServerErrorException.class,
                () -> client.get()
                        .uri("/api/lending/loans")
                        .header("Authorization", "Bearer " + token("STUDENT"))
                        .retrieve()
                        .toEntity(String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        lendingStub.verify(1, getRequestedFor(urlPathEqualTo("/api/lending/loans")));

    }
}