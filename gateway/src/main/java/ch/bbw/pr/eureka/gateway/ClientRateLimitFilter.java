package ch.bbw.pr.eureka.gateway;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** A local, fixed-window quota for each remote IP address. No external store is required. */
@Component
public class ClientRateLimitFilter implements GlobalFilter, Ordered {
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_MILLIS = 60_000;

    private final Clock clock;
    private final Map<String, Quota> quotas = new HashMap<>();
    private long lastCleanupWindow = -1;

    public ClientRateLimitFilter() {
        this(Clock.systemUTC());
    }

    ClientRateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // CORS preflights must not consume a browser's request quota.
        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        String client = remote == null ? "unknown" : remote.getHostString();
        if (allow(client)) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private synchronized boolean allow(String client) {
        long window = Math.floorDiv(clock.millis(), WINDOW_MILLIS);
        if (window != lastCleanupWindow) {
            quotas.entrySet().removeIf(entry -> entry.getValue().window != window);
            lastCleanupWindow = window;
        }
        Quota quota = quotas.computeIfAbsent(client, ignored -> new Quota(window));
        if (quota.count >= REQUESTS_PER_MINUTE) {
            return false;
        }
        quota.count++;
        return true;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private static final class Quota {
        private final long window;
        private int count;

        private Quota(long window) {
            this.window = window;
        }
    }
}
