package com.example.battleship.webSocket;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameWebSocketMetrics {

    private final AtomicInteger activeSessions = new AtomicInteger();
    private final Counter registrationsCounter;
    private final Counter heartbeatSentCounter;
    private final Counter heartbeatAckCounter;
    private final Counter inactiveSessionClosedCounter;

    public GameWebSocketMetrics(MeterRegistry meterRegistry) {
        Gauge.builder("battleship_websocket_sessions_active", activeSessions, AtomicInteger::get)
                .description("Current active websocket sessions registered by the game")
                .register(meterRegistry);

        this.registrationsCounter = Counter.builder("battleship_websocket_sessions_registered_total")
                .description("Total websocket session registrations")
                .register(meterRegistry);

        this.heartbeatSentCounter = Counter.builder("battleship_websocket_heartbeats_sent_total")
                .description("Total heartbeat messages sent to websocket sessions")
                .register(meterRegistry);

        this.heartbeatAckCounter = Counter.builder("battleship_websocket_heartbeats_acked_total")
                .description("Total heartbeat acknowledgements received from websocket sessions")
                .register(meterRegistry);

        this.inactiveSessionClosedCounter = Counter.builder("battleship_websocket_sessions_timed_out_total")
                .description("Total websocket sessions closed after inactivity timeout")
                .register(meterRegistry);
    }

    public void recordSessionRegistered() {
        registrationsCounter.increment();
        activeSessions.incrementAndGet();
    }

    public void recordSessionClosed() {
        activeSessions.updateAndGet(value -> Math.max(0, value - 1));
    }

    public void recordHeartbeatSent() {
        heartbeatSentCounter.increment();
    }

    public void recordHeartbeatAck() {
        heartbeatAckCounter.increment();
    }

    public void recordInactiveSessionClosed() {
        inactiveSessionClosedCounter.increment();
    }
}