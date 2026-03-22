package org.aeza.aezaserver.repository;

import org.aeza.aezaserver.model.Agent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, String> {
    List<Agent> findAllByOrderByLastSeenDesc();

    List<Agent> findAllByGroupIdOrderByLastSeenDesc(Long groupId);

    List<Agent> findAllByAgentIdInOrderByLastSeenDesc(Collection<String> agentIds);

    @Query("select a.agentId from Agent a where a.group.id = :groupId order by a.agentId")
    List<String> findAgentIdsByGroupId(@Param("groupId") Long groupId);
}
