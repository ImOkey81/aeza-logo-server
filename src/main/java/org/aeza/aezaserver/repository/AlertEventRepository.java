package org.aeza.aezaserver.repository;

import org.aeza.aezaserver.model.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    List<AlertEvent> findAllByOrderByTriggeredAtDesc();
}
