package com.xdata.controller.admin;

import com.xdata.model.LmsCredential;
import com.xdata.repository.LmsCredentialRepository;
import com.xdata.service.AccessControlService;
import com.xdata.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/lms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
public class LmsController {
    private final LmsCredentialRepository lmsCredentialRepository;
    private final AccessControlService accessControlService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<LmsCredential>> getAllCredentials() {
        return ResponseEntity.ok(lmsCredentialRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<LmsCredential> createCredential(@RequestBody LmsCredential credential) {
        if (!accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        LmsCredential saved = lmsCredentialRepository.save(credential);
        auditService.log("LMS_CREDENTIAL_CREATED", saved.getLmsName(), "ID: " + saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LmsCredential> updateCredential(@PathVariable Integer id, @RequestBody LmsCredential credential) {
        if (!accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        credential.setId(id);
        LmsCredential saved = lmsCredentialRepository.save(credential);
        auditService.log("LMS_CREDENTIAL_UPDATED", saved.getLmsName(), "ID: " + saved.getId());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable Integer id) {
        if (!accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        lmsCredentialRepository.deleteById(id);
        auditService.log("LMS_CREDENTIAL_DELETED", "ID: " + id, "");
        return ResponseEntity.ok().build();
    }
}
