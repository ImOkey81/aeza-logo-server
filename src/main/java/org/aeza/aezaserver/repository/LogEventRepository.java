package org.aeza.aezaserver.repository;

import org.aeza.aezaserver.model.LogEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEventRepository extends JpaRepository<LogEventEntity, Long> {
}
