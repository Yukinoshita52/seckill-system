// Copyright (c) 2026 seckill-system
package com.seckill.engine.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "seckill-engine");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
