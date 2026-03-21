package org.aeza.aezaserver.repository;

import org.aeza.aezaserver.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, String> {
    List<Agent> findAllByOrderByLastSeenDesc();
}
