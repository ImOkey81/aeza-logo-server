package org.aeza.aezaserver.controller;

import org.aeza.aezaserver.dto.search.SearchResponseDto;
import org.aeza.aezaserver.service.LiveStreamService;
import org.aeza.aezaserver.service.SearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class SearchController {
    private final SearchService searchService;
    private final LiveStreamService liveStreamService;

    public SearchController(SearchService searchService, LiveStreamService liveStreamService) {
        this.searchService = searchService;
        this.liveStreamService = liveStreamService;
    }

    @GetMapping("/logs/search")
    public SearchResponseDto search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return searchService.search(q, host, service, level, from, to, page, size);
    }

    @GetMapping("/live/sse")
    public SseEmitter live() {
        return liveStreamService.subscribe();
    }
}
