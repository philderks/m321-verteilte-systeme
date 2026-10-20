package ch.bbw.pr.eureka.gateway;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "eureka.client.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayApplicationTests {
    @Autowired RouteDefinitionLocator routeDefinitions;
    @Autowired WebTestClient client;

    @Test
    void apiRouteTargetsService02AndRemovesOnePrefix() {
        RouteDefinition route = route("service-02");
        assertThat(route.getUri().toString()).isEqualTo("lb://service-02");
        assertThat(route.getPredicates()).anySatisfy(predicate ->
                assertThat(predicate.getArgs()).containsValue("/api/**"));
        assertThat(route.getFilters()).anySatisfy(filter -> {
            assertThat(filter.getName()).isEqualTo("StripPrefix");
            assertThat(filter.getArgs()).containsValue("1");
        });
    }

    @Test
    void service01RouteUsesDistinctPathAndEureka() {
        RouteDefinition route = route("service-01");
        assertThat(route.getUri().toString()).isEqualTo("lb://service-01");
        assertThat(route.getPredicates()).anySatisfy(predicate ->
                assertThat(predicate.getArgs()).containsValue("/service-01/**"));
        assertThat(route.getFilters()).anySatisfy(filter -> {
            assertThat(filter.getName()).isEqualTo("StripPrefix");
            assertThat(filter.getArgs()).containsValue("1");
        });
    }

    @Test
    void browserPreflightAllowsFrontendOrigin() {
        client.options().uri("/api/call")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
    }

    @Test
    void browserPreflightRejectsOtherOrigins() {
        client.options().uri("/api/books")
                .header("Origin", "https://not-allowed.example")
                .header("Access-Control-Request-Method", "GET")
                .exchange().expectStatus().isForbidden();
    }

    private RouteDefinition route(String id) {
        List<RouteDefinition> definitions = routeDefinitions.getRouteDefinitions().collectList().block();
        assertThat(definitions).isNotNull();
        return definitions.stream().filter(definition -> definition.getId().equals(id)).findFirst().orElseThrow();
    }
}
