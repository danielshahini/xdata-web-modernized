package com.xdata.controller;

import com.xdata.service.PublicSandboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public, login-free SQL sandbox ("Try SQL", W3Schools-style). Everything here is
 * read-only and runs against an ephemeral in-memory database — see
 * {@link PublicSandboxService}. Permitted anonymously in SecurityConfiguration.
 */
@RestController
@RequestMapping("/api/v1/public/sql")
@RequiredArgsConstructor
public class PublicSqlController {

    private final PublicSandboxService sandbox;

    /** Sample schema + starter queries for the sandbox UI. */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
                "schema", sandbox.schemaMetadata(),
                "examples", sandbox.exampleQueries()
        ));
    }

    /** Run a read-only SELECT against the throwaway sample database. */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> run(@RequestBody Map<String, Object> body) {
        String query = body != null ? (String) body.get("query") : null;
        return ResponseEntity.ok(sandbox.run(query));
    }
}
