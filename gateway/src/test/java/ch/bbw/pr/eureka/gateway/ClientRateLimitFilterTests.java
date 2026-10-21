package ch.bbw.pr.eureka.gateway;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class ClientRateLimitFilterTests {
    private final AtomicLong time = new AtomicLong(0);
    private final Clock clock = new Clock() {
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(time.get()); }
        @Override public long millis() { return time.get(); }
    };
    private final ClientRateLimitFilter filter = new ClientRateLimitFilter(clock);
    private final AtomicInteger forwarded = new AtomicInteger();
    private final GatewayFilterChain chain = exchange -> Mono.fromRunnable(forwarded::incrementAndGet);

    @Test
    void differentClientHasIndependentQuota() {
        for (int i = 0; i < 5; i++) {
            assertThat(request("127.0.0.1").getResponse().getStatusCode()).isNull();
        }
        assertThat(request("127.0.0.1").getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(request("127.0.0.2").getResponse().getStatusCode()).isNull();
        assertThat(forwarded).hasValue(6);
    }

    @Test
    void quotaResetsAtNextMinuteWithoutSleeping() {
        for (int i = 0; i < 5; i++) {
            request("127.0.0.1");
        }
        time.set(60_000);
        assertThat(request("127.0.0.1").getResponse().getStatusCode()).isNull();
        assertThat(forwarded).hasValue(6);
    }

    @Test
    void optionsDoesNotConsumeQuota() {
        for (int i = 0; i < 10; i++) {
            var preflight = MockServerWebExchange.from(MockServerHttpRequest.options("/api/books")
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 12345)));
            filter.filter(preflight, chain).block();
            assertThat(preflight.getResponse().getStatusCode()).isNull();
        }
        assertThat(request("127.0.0.1").getResponse().getStatusCode()).isNull();
    }

    private MockServerWebExchange request(String ip) {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/books")
                .remoteAddress(new InetSocketAddress(ip, 12345)));
        filter.filter(exchange, chain).block();
        return exchange;
    }
}
