package org.aeza.aezaserver.repository;

import org.aeza.aezaserver.model.AgentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentGroupRepository extends JpaRepository<AgentGroup, Long> {
    Optional<AgentGroup> findByNameIgnoreCase(String name);
}
