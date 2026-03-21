package org.aeza.aezaserver.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aeza.aezaserver.dto.ingest.LogEventDto;
import org.aeza.aezaserver.model.LogEventEntity;
import org.aeza.aezaserver.repository.LogEventRepository;
import org.aeza.aezaserver.service.SearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OpenSearchClientAdapter {

    private final LogEventRepository logEventRepository;
    private final ObjectMapper objectMapper;

    public OpenSearchClientAdapter(LogEventRepository logEventRepository, ObjectMapper objectMapper) {
        this.logEventRepository = logEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void bulkIndex(String agentId, List<LogEventDto> events) {
        List<LogEventEntity> entities = events.stream().map(event -> {
            LogEventEntity entity = new LogEventEntity();
            entity.setTimestamp(event.timestamp());
            entity.setLevel(normalize(event.level()));
            entity.setMessage(event.message());
            entity.setHost(event.host());
            entity.setService(event.service());
            entity.setSourceType(event.sourceType());
            entity.setSourcePath(event.sourcePath());
            entity.setTags(writeJson(event.tags() == null ? List.of() : event.tags()));
            entity.setMetadata(writeJson(event.metadata() == null ? Map.of() : event.metadata()));
            entity.setAgentId(event.agentId() == null || event.agentId().isBlank() ? agentId : event.agentId());
            return entity;
        }).toList();

        logEventRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public SearchResult search(SearchCriteria criteria, int page, int size) {
        List<LogDocument> filtered = applyFilters(criteria);
        int from = Math.max(page, 0) * Math.max(size, 1);
        int to = Math.min(from + Math.max(size, 1), filtered.size());
        List<LogDocument> items = from >= filtered.size() ? List.of() : filtered.subList(from, to);

        Map<String, Object> aggs = new HashMap<>();
        aggs.put("levels", termsAgg(filtered, LogDocument::level));
        aggs.put("hosts", termsAgg(filtered, LogDocument::host));
        aggs.put("services", termsAgg(filtered, LogDocument::service));

        return new SearchResult(items, filtered.size(), aggs);
    }

    @Transactional(readOnly = true)
    public long count(SearchCriteria criteria) {
        return applyFilters(criteria).size();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> topHosts(SearchCriteria criteria, int limit) {
        return termsAgg(applyFilters(criteria), LogDocument::host).entrySet().stream()
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> topServices(SearchCriteria criteria, int limit) {
        return termsAgg(applyFilters(criteria), LogDocument::service).entrySet().stream()
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    @Transactional(readOnly = true)
    public List<TimeseriesBucket> timeseries(SearchCriteria criteria) {
        Map<Instant, long[]> buckets = new HashMap<>();
        for (LogDocument doc : applyFilters(criteria)) {
            Instant key = doc.timestamp().truncatedTo(ChronoUnit.MINUTES);
            long[] values = buckets.computeIfAbsent(key, ignored -> new long[2]);
            if ("ERROR".equals(doc.level())) {
                values[0]++;
            }
            if ("WARN".equals(doc.level()) || "WARNING".equals(doc.level())) {
                values[1]++;
            }
        }
        return buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new TimeseriesBucket(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    @Transactional(readOnly = true)
    public long uniqueHosts(SearchCriteria criteria) {
        return applyFilters(criteria).stream().map(LogDocument::host).distinct().count();
    }

    @Transactional(readOnly = true)
    public long uniqueServices(SearchCriteria criteria) {
        return applyFilters(criteria).stream().map(LogDocument::service).distinct().count();
    }

    private List<LogDocument> applyFilters(SearchCriteria criteria) {
        return new ArrayList<>(logEventRepository.findAll()).stream()
                .map(this::toDocument)
                .filter(doc -> criteria.from() == null || !doc.timestamp().isBefore(criteria.from()))
                .filter(doc -> criteria.to() == null || !doc.timestamp().isAfter(criteria.to()))
                .filter(doc -> matches(doc.host(), criteria.host()))
                .filter(doc -> matches(doc.service(), criteria.service()))
                .filter(doc -> matches(normalize(doc.level()), normalize(criteria.level())))
                .filter(doc -> matchesQuery(doc, criteria.q()))
                .sorted(Comparator.comparing(LogDocument::timestamp).reversed())
                .toList();
    }

    private LogDocument toDocument(LogEventEntity entity) {
        return new LogDocument(
                entity.getTimestamp(),
                entity.getLevel(),
                entity.getMessage(),
                entity.getHost(),
                entity.getService(),
                entity.getSourceType(),
                entity.getSourcePath(),
                readTags(entity.getTags()),
                readMetadata(entity.getMetadata()),
                entity.getAgentId()
        );
    }

    private boolean matches(String actual, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return Objects.equals(normalize(actual), normalize(expected));
    }

    private boolean matchesQuery(LogDocument doc, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String lower = q.toLowerCase(Locale.ROOT);
        return doc.message().toLowerCase(Locale.ROOT).contains(lower)
                || doc.host().toLowerCase(Locale.ROOT).contains(lower)
                || doc.service().toLowerCase(Locale.ROOT).contains(lower);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private Map<String, Long> termsAgg(List<LogDocument> docs, java.util.function.Function<LogDocument, String> extractor) {
        return docs.stream()
                .collect(Collectors.groupingBy(extractor, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
            return "{}";
        }
    }

    private List<String> readTags(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
            return List.of();
        }
    }

    private Map<String, Object> readMetadata(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
            return Map.of();
        }
    }

    public record SearchResult(List<LogDocument> items, long total, Map<String, Object> aggregations) {
    }

    public record TimeseriesBucket(Instant timestamp, long errors, long warnings) {
    }

    public record LogDocument(
            Instant timestamp,
            String level,
            String message,
            String host,
            String service,
            String sourceType,
            String sourcePath,
            List<String> tags,
            Map<String, Object> metadata,
            String agentId
    ) {
    }
}
