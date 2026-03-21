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
@Table(name = "alert_events")
@Getter
@Setter
@NoArgsConstructor
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false)
    private long value;

    @Column(nullable = false)
    private long threshold;
}
