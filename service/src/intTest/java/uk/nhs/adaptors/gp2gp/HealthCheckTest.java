package uk.nhs.adaptors.gp2gp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.gp2gp.testcontainers.ActiveMQExtension;
import uk.nhs.adaptors.gp2gp.testcontainers.MongoDBExtension;

import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({SpringExtension.class, MongoDBExtension.class, ActiveMQExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class HealthCheckTest {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";

    @LocalServerPort
    private int port;

    @Test
    public void When_GettingHealthCheck_Expect_OkStatusResponse() {
        given()
                .port(port)
                .when()
                .get(HEALTHCHECK_ENDPOINT)
                .then()
                .statusCode(OK.value());
    }
}
