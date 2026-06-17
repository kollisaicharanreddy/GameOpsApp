package com.gameops.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SystemController {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @GetMapping("/db-info")
    public ResponseEntity<Map<String, String>> getDbInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("activeProfile", activeProfile);
        
        String dbType = "PostgreSQL";
        String displayUrl = datasourceUrl;
        
        if (datasourceUrl.contains("h2:mem")) {
            dbType = "H2 (In-Memory)";
        } else if (datasourceUrl.contains("neon.tech")) {
            dbType = "NeonDB (Cloud PostgreSQL)";
            // Extract the domain for cleaner display
            try {
                int start = datasourceUrl.indexOf("//") + 2;
                int end = datasourceUrl.indexOf("/", start);
                if (end > start) {
                    displayUrl = datasourceUrl.substring(start, end);
                }
            } catch (Exception e) {
                // fallback
            }
        }
        
        info.put("dbType", dbType);
        info.put("url", displayUrl);
        return ResponseEntity.ok(info);
    }
}
