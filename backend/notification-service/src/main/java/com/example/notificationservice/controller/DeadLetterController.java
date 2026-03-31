package com.example.notificationservice.controller;

import com.example.notificationservice.entity.DeadLetterMessage;
import com.example.notificationservice.service.DeadLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dlq")
@RequiredArgsConstructor
public class DeadLetterController {

    private final DeadLetterService deadLetterService;

    @GetMapping
    public ResponseEntity<List<DeadLetterMessage>> getPendingMessages() {
        return ResponseEntity.ok(deadLetterService.getPendingMessages());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> markRetried(@PathVariable Long id) {
        deadLetterService.markRetried(id);
        return ResponseEntity.ok().build();
    }
}
