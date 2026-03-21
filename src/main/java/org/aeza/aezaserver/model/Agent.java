package org.aeza.aezaserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
public class Agent {

    @Id
    @Column(name = "agent_id", nullable = false, length = 128)
    private String agentId;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "buffered_count", nullable = false)
    private int bufferedCount;
}
