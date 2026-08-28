package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/api/ping")
    public ResponseEntity<Map<String, Object>> apiPing() {
        return ResponseEntity.ok(Map.of(
                "message", "pong",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
