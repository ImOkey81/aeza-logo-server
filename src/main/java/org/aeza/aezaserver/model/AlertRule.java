package org.aeza.aezaserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "alert_rules")
@Getter
@Setter
@NoArgsConstructor
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "condition_type", nullable = false, length = 64)
    private String conditionType;

    @Column(name = "query_text")
    private String query;

    @Column(nullable = false)
    private long threshold;

    @Column(name = "window_seconds", nullable = false)
    private long windowSeconds;

    @Column(name = "cooldown_seconds", nullable = false)
    private long cooldownSeconds;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(length = 32)
    private String level;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;
}
