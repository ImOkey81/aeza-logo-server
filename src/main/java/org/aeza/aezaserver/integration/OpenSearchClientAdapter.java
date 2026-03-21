package org.aeza.aezaserver.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aeza.aezaserver.config.OpenSearchProperties;
import org.aeza.aezaserver.dto.ingest.LogEventDto;
import org.aeza.aezaserver.model.LogEventEntity;
import org.aeza.aezaserver.repository.LogEventRepository;
import org.aeza.aezaserver.service.SearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    private static final Logger log = LoggerFactory.getLogger(OpenSearchClientAdapter.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final int LEGACY_HOST_MAX = 255;
    private static final int LEGACY_SERVICE_MAX = 255;
    private static final int LEGACY_SOURCE_TYPE_MAX = 128;
    private static final int LEGACY_SOURCE_PATH_MAX = 1024;
    private static final int LEGACY_AGENT_ID_MAX = 128;

    private final LogEventRepository logEventRepository;
    private final ObjectMapper objectMapper;
    private final RestClient openSearchRestClient;
    private final OpenSearchProperties openSearchProperties;

    public OpenSearchClientAdapter(
            LogEventRepository logEventRepository,
            ObjectMapper objectMapper,
            RestClient openSearchRestClient,
            OpenSearchProperties openSearchProperties
    ) {
        this.logEventRepository = logEventRepository;
        this.objectMapper = objectMapper;
        this.openSearchRestClient = openSearchRestClient;
        this.openSearchProperties = openSearchProperties;
    }

    @Transactional
    public void bulkIndex(String agentId, List<LogEventDto> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<LogEventEntity> entities = events.stream().map(event -> {
            LogEventEntity entity = new LogEventEntity();
            entity.setTimestamp(event.timestamp());
            entity.setLevel(normalize(event.level()));
            entity.setMessage(event.message());
            entity.setHost(trimToDbLimit(event.host(), LEGACY_HOST_MAX));
            entity.setService(trimToDbLimit(event.service(), LEGACY_SERVICE_MAX));
            entity.setSourceType(trimToDbLimit(event.sourceType(), LEGACY_SOURCE_TYPE_MAX));
            entity.setSourcePath(trimToDbLimit(event.sourcePath(), LEGACY_SOURCE_PATH_MAX));
            entity.setTags(writeJson(event.tags() == null ? List.of() : event.tags()));
            entity.setMetadata(writeJson(event.metadata() == null ? Map.of() : event.metadata()));
            entity.setAgentId(trimToDbLimit(
                    event.agentId() == null || event.agentId().isBlank() ? agentId : event.agentId(),
                    LEGACY_AGENT_ID_MAX
            ));
            return entity;
        }).toList();

        // Keep Postgres as durable local storage.
        logEventRepository.saveAll(entities);

        if (!openSearchProperties.enabled()) {
            return;
        }

        StringBuilder ndjson = new StringBuilder(events.size() * 256);
        for (LogEventDto event : events) {
            String resolvedAgentId = event.agentId() == null || event.agentId().isBlank() ? agentId : event.agentId();
            String indexName = indexForTimestamp(event.timestamp());

            ndjson.append("{\"index\":{\"_index\":\"")
                    .append(indexName)
                    .append("\"}}\n");

            Map<String, Object> doc = Map.of(
                    "timestamp", event.timestamp().toString(),
                    "level", normalize(event.level()),
                    "message", event.message(),
                    "host", event.host(),
                    "service", event.service(),
                    "sourceType", event.sourceType(),
                    "sourcePath", event.sourcePath(),
                    "tags", event.tags() == null ? List.of() : event.tags(),
                    "metadata", event.metadata() == null ? Map.of() : event.metadata(),
                    "agentId", resolvedAgentId
            );

            ndjson.append(writeJson(doc)).append('\n');
        }

        try {
            String response = openSearchRestClient.post()
                    .uri("/_bulk")
                    .contentType(MediaType.APPLICATION_NDJSON)
                    .body(ndjson.toString())
                    .retrieve()
                    .body(String.class);

            Map<String, Object> parsed = parseMap(response);
            if (Boolean.TRUE.equals(parsed.get("errors"))) {
                log.warn("OpenSearch bulk indexing returned item-level errors");
            }
        } catch (RestClientException ex) {
            log.warn("OpenSearch bulk indexing failed, data remains in Postgres fallback: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SearchResult search(SearchCriteria criteria, int page, int size) {
        if (!openSearchProperties.enabled()) {
            return searchFromPostgres(criteria, page, size);
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        Map<String, Object> request = new HashMap<>();
        request.put("from", safePage * safeSize);
        request.put("size", safeSize);
        request.put("sort", List.of(Map.of("timestamp", Map.of("order", "desc"))));
        request.put("query", buildQuery(criteria));
        request.put("aggs", Map.of(
                "levels", Map.of("terms", Map.of("field", "level.keyword", "size", 20)),
                "hosts", Map.of("terms", Map.of("field", "host.keyword", "size", 50)),
                "services", Map.of("terms", Map.of("field", "service.keyword", "size", 50))
        ));

        try {
            Map<String, Object> response = searchRequest(request);
            List<LogDocument> items = parseHits(response);
            long total = extractTotal(response);
            Map<String, Object> aggregations = parseSearchAggregations(response);
            return new SearchResult(items, total, aggregations);
        } catch (Exception ex) {
            log.warn("OpenSearch search failed, using Postgres fallback: {}", ex.getMessage());
            return searchFromPostgres(criteria, page, size);
        }
    }

    @Transactional(readOnly = true)
    public long count(SearchCriteria criteria) {
        if (!openSearchProperties.enabled()) {
            return applyFilters(criteria).size();
        }

        Map<String, Object> request = Map.of("query", buildQuery(criteria));

        try {
            String raw = openSearchRestClient.post()
                    .uri("/" + indexPattern() + "/_count")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(writeJson(request))
                    .retrieve()
                    .body(String.class);

            Map<String, Object> parsed = parseMap(raw);
            Object count = parsed.get("count");
            return count instanceof Number n ? n.longValue() : 0L;
        } catch (Exception ex) {
            log.warn("OpenSearch count failed, using Postgres fallback: {}", ex.getMessage());
            return applyFilters(criteria).size();
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Long> topHosts(SearchCriteria criteria, int limit) {
        return termsAggregation("host.keyword", criteria, limit, this::topHostsFallback, "topHosts");
    }

    @Transactional(readOnly = true)
    public Map<String, Long> topServices(SearchCriteria criteria, int limit) {
        return termsAggregation("service.keyword", criteria, limit, this::topServicesFallback, "topServices");
    }

    @Transactional(readOnly = true)
    public List<TimeseriesBucket> timeseries(SearchCriteria criteria) {
        if (!openSearchProperties.enabled()) {
            return timeseriesFallback(criteria);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("size", 0);
        request.put("query", buildQuery(criteria));
        request.put("aggs", Map.of(
                "series", Map.of(
                        "date_histogram", Map.of(
                                "field", "timestamp",
                                "fixed_interval", "1m",
                                "min_doc_count", 0
                        ),
                        "aggs", Map.of(
                                "errors", Map.of("filter", Map.of("term", Map.of("level.keyword", "ERROR"))),
                                "warnings", Map.of("filter", Map.of("terms", Map.of("level.keyword", List.of("WARN", "WARNING"))))
                        )
                )
        ));

        try {
            Map<String, Object> response = searchRequest(request);
            Map<String, Object> aggs = asMap(response.get("aggregations"));
            Map<String, Object> series = asMap(aggs.get("series"));
            List<Map<String, Object>> buckets = asListOfMaps(series.get("buckets"));

            return buckets.stream().map(bucket -> {
                Instant ts = Instant.parse(String.valueOf(bucket.get("key_as_string")));
                long errors = extractDocCount(asMap(bucket.get("errors")));
                long warnings = extractDocCount(asMap(bucket.get("warnings")));
                return new TimeseriesBucket(ts, errors, warnings);
            }).toList();
        } catch (Exception ex) {
            log.warn("OpenSearch timeseries failed, using Postgres fallback: {}", ex.getMessage());
            return timeseriesFallback(criteria);
        }
    }

    @Transactional(readOnly = true)
    public long uniqueHosts(SearchCriteria criteria) {
        return cardinality("host.keyword", criteria, this::uniqueHostsFallback, "uniqueHosts");
    }

    @Transactional(readOnly = true)
    public long uniqueServices(SearchCriteria criteria) {
        return cardinality("service.keyword", criteria, this::uniqueServicesFallback, "uniqueServices");
    }

    private Map<String, Long> termsAggregation(
            String field,
            SearchCriteria criteria,
            int limit,
            java.util.function.Function<SearchCriteria, Map<String, Long>> fallback,
            String op
    ) {
        if (!openSearchProperties.enabled()) {
            return fallback.apply(criteria);
        }

        int safeLimit = Math.max(1, limit);

        Map<String, Object> request = new HashMap<>();
        request.put("size", 0);
        request.put("query", buildQuery(criteria));
        request.put("aggs", Map.of(
                "values", Map.of("terms", Map.of("field", field, "size", safeLimit))
        ));

        try {
            Map<String, Object> response = searchRequest(request);
            Map<String, Object> aggs = asMap(response.get("aggregations"));
            Map<String, Object> values = asMap(aggs.get("values"));
            List<Map<String, Object>> buckets = asListOfMaps(values.get("buckets"));

            return buckets.stream().collect(Collectors.toMap(
                    bucket -> String.valueOf(bucket.get("key")),
                    bucket -> ((Number) bucket.getOrDefault("doc_count", 0)).longValue(),
                    (a, b) -> a,
                    java.util.LinkedHashMap::new
            ));
        } catch (Exception ex) {
            log.warn("OpenSearch {} failed, using Postgres fallback: {}", op, ex.getMessage());
            return fallback.apply(criteria);
        }
    }

    private long cardinality(
            String field,
            SearchCriteria criteria,
            java.util.function.ToLongFunction<SearchCriteria> fallback,
            String op
    ) {
        if (!openSearchProperties.enabled()) {
            return fallback.applyAsLong(criteria);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("size", 0);
        request.put("query", buildQuery(criteria));
        request.put("aggs", Map.of(
                "values", Map.of("cardinality", Map.of("field", field))
        ));

        try {
            Map<String, Object> response = searchRequest(request);
            Map<String, Object> aggs = asMap(response.get("aggregations"));
            Map<String, Object> values = asMap(aggs.get("values"));
            Object val = values.get("value");
            return val instanceof Number number ? number.longValue() : 0L;
        } catch (Exception ex) {
            log.warn("OpenSearch {} failed, using Postgres fallback: {}", op, ex.getMessage());
            return fallback.applyAsLong(criteria);
        }
    }

    private SearchResult searchFromPostgres(SearchCriteria criteria, int page, int size) {
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

    private Map<String, Long> topHostsFallback(SearchCriteria criteria) {
        return termsAgg(applyFilters(criteria), LogDocument::host);
    }

    private Map<String, Long> topServicesFallback(SearchCriteria criteria) {
        return termsAgg(applyFilters(criteria), LogDocument::service);
    }

    private List<TimeseriesBucket> timeseriesFallback(SearchCriteria criteria) {
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

    private long uniqueHostsFallback(SearchCriteria criteria) {
        return applyFilters(criteria).stream().map(LogDocument::host).distinct().count();
    }

    private long uniqueServicesFallback(SearchCriteria criteria) {
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

    private Map<String, Object> searchRequest(Map<String, Object> requestBody) {
        String raw = openSearchRestClient.post()
                .uri("/" + indexPattern() + "/_search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(writeJson(requestBody))
                .retrieve()
                .body(String.class);

        return parseMap(raw);
    }

    private Map<String, Object> buildQuery(SearchCriteria criteria) {
        List<Map<String, Object>> filters = new ArrayList<>();

        if (criteria.from() != null || criteria.to() != null) {
            Map<String, Object> range = new HashMap<>();
            if (criteria.from() != null) {
                range.put("gte", criteria.from().toString());
            }
            if (criteria.to() != null) {
                range.put("lte", criteria.to().toString());
            }
            filters.add(Map.of("range", Map.of("timestamp", range)));
        }

        if (hasText(criteria.host())) {
            filters.add(Map.of("term", Map.of("host.keyword", criteria.host())));
        }

        if (hasText(criteria.service())) {
            filters.add(Map.of("term", Map.of("service.keyword", criteria.service())));
        }

        if (hasText(criteria.level())) {
            filters.add(Map.of("term", Map.of("level.keyword", normalize(criteria.level()))));
        }

        if (hasText(criteria.q())) {
            filters.add(Map.of(
                    "multi_match",
                    Map.of(
                            "query", criteria.q(),
                            "fields", List.of("message", "host", "service")
                    )
            ));
        }

        if (filters.isEmpty()) {
            return Map.of("match_all", Map.of());
        }

        return Map.of("bool", Map.of("filter", filters));
    }

    private List<LogDocument> parseHits(Map<String, Object> response) {
        Map<String, Object> hits = asMap(response.get("hits"));
        List<Map<String, Object>> rawHits = asListOfMaps(hits.get("hits"));

        return rawHits.stream().map(hit -> {
            Map<String, Object> source = asMap(hit.get("_source"));
            return new LogDocument(
                    parseInstant(source.get("timestamp")),
                    asString(source.get("level")),
                    asString(source.get("message")),
                    asString(source.get("host")),
                    asString(source.get("service")),
                    asString(source.get("sourceType")),
                    asString(source.get("sourcePath")),
                    asListOfStrings(source.get("tags")),
                    asMap(source.get("metadata")),
                    asString(source.get("agentId"))
            );
        }).toList();
    }

    private long extractTotal(Map<String, Object> response) {
        Map<String, Object> hits = asMap(response.get("hits"));
        Object totalObj = hits.get("total");
        if (totalObj instanceof Map<?, ?> map) {
            Object value = map.get("value");
            return value instanceof Number number ? number.longValue() : 0L;
        }
        if (totalObj instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private Map<String, Object> parseSearchAggregations(Map<String, Object> response) {
        Map<String, Object> aggs = asMap(response.get("aggregations"));

        return Map.of(
                "levels", parseTermsBuckets(asMap(aggs.get("levels"))),
                "hosts", parseTermsBuckets(asMap(aggs.get("hosts"))),
                "services", parseTermsBuckets(asMap(aggs.get("services")))
        );
    }

    private Map<String, Long> parseTermsBuckets(Map<String, Object> termsNode) {
        List<Map<String, Object>> buckets = asListOfMaps(termsNode.get("buckets"));
        return buckets.stream().collect(Collectors.toMap(
                bucket -> String.valueOf(bucket.get("key")),
                bucket -> ((Number) bucket.getOrDefault("doc_count", 0)).longValue(),
                (a, b) -> a,
                java.util.LinkedHashMap::new
        ));
    }

    private long extractDocCount(Map<String, Object> node) {
        Object count = node.get("doc_count");
        return count instanceof Number n ? n.longValue() : 0L;
    }

    private String indexForTimestamp(Instant timestamp) {
        String date = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                .withLocale(Locale.ROOT)
                .withZone(ZoneOffset.UTC)
                .format(timestamp);
        return openSearchProperties.indexPrefix() + "-" + date;
    }

    private String indexPattern() {
        return openSearchProperties.indexPrefix() + "-*";
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean matches(String actual, String expected) {
        if (!hasText(expected)) {
            return true;
        }
        return Objects.equals(normalize(actual), normalize(expected));
    }

    private boolean matchesQuery(LogDocument doc, String q) {
        if (!hasText(q)) {
            return true;
        }
        String lower = q.toLowerCase(Locale.ROOT);
        return doc.message().toLowerCase(Locale.ROOT).contains(lower)
                || doc.host().toLowerCase(Locale.ROOT).contains(lower)
                || doc.service().toLowerCase(Locale.ROOT).contains(lower);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize json", ex);
        }
    }

    private Map<String, Object> parseMap(String raw) {
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to parse OpenSearch response", ex);
        }
    }

    private String trimToDbLimit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        log.warn("Truncating oversized log field from {} to {} characters for Postgres storage", value.length(), maxLength);
        return value.substring(0, maxLength);
    }

    private List<String> readTags(String value) {
        if (!hasText(value)) {
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
        if (!hasText(value)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
            return Map.of();
        }
    }

    private Instant parseInstant(Object value) {
        if (value == null) {
            return Instant.EPOCH;
        }
        return Instant.parse(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> raw) {
            Map<String, Object> out = new HashMap<>();
            raw.forEach((k, v) -> out.put(String.valueOf(k), v));
            return out;
        }
        return Map.of();
    }

    private List<Map<String, Object>> asListOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(this::asMap).toList();
    }

    private List<String> asListOfStrings(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
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
