package com.example.finance.controller;

import com.example.finance.dto.response.ApiResponse;
import com.example.finance.service.wiki.WikiIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/wiki")
@RequiredArgsConstructor
public class AiWikiController {

    private final WikiIngestionService wikiIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ingest() {
        WikiIngestionService.IngestSummary summary = wikiIngestionService.ingestPendingSources();
        return ResponseEntity
                .ok(ApiResponse.success("Wiki ingest completed", Map.of("filesIngested", summary.filesIngested(),
                        "pagesCreated", summary.pagesCreated(), "pagesUpdated", summary.pagesUpdated())));
    }

    @PostMapping("/lint")
    public ResponseEntity<ApiResponse<Map<String, String>>> lint() {
        String reportPath = wikiIngestionService.lintWiki();
        return ResponseEntity.ok(ApiResponse.success("Wiki lint completed", Map.of("reportPath", reportPath)));
    }
}
