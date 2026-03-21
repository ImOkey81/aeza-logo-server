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
@Table(name = "log_events")
@Getter
@Setter
@NoArgsConstructor
public class LogEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 32)
    private String level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 2048)
    private String host;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String service;

    @Column(name = "source_type", length = 255)
    private String sourceType;

    @Column(name = "source_path", columnDefinition = "TEXT")
    private String sourcePath;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "agent_id", nullable = false, length = 255)
    private String agentId;
}
