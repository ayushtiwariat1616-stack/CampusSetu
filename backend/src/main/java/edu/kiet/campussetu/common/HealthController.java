package edu.kiet.campussetu.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    @Value("${spring.application.name:CampusSetu}")
    private String appName;

    public HealthController(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbHealthy = false;
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbHealthy = (result != null && result == 1);
        } catch (Exception e) {
            dbHealthy = false;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        // Preserve existing contract expected by frontends / health checks
        resp.put("application", "CampusSetu");
        resp.put("status", dbHealthy ? "UP" : "DEGRADED");
        resp.put("message", "React successfully connected to Spring Boot.");
        resp.put("database", dbHealthy ? "CONNECTED" : "UNREACHABLE");
        resp.put("timestamp", Instant.now().toString());
        resp.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));

        return ResponseEntity.ok(resp);
    }
}
