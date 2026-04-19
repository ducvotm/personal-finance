package com.example.finance.controller;

import com.example.finance.dto.request.AiAssistantQueryRequest;
import com.example.finance.dto.response.AiAssistantResponse;
import com.example.finance.dto.response.ApiResponse;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AiAssistantResponse>> query(Authentication authentication, @Valid @RequestBody
    AiAssistantQueryRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        AiAssistantResponse response = aiAssistantService.query(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Assistant response generated", response));
    }
}
