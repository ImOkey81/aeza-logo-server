package org.aeza.aezaserver.repository;

import org.aeza.aezaserver.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByEnabledTrueOrderByIdAsc();
}
