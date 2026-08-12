package com.datashare.backend.controller;

import com.datashare.backend.dto.ShareRequest;
import com.datashare.backend.dto.ShareResponse;
import com.datashare.backend.service.ShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * POST /api/share — partage un fichier déjà uploadé avec un destinataire (email).
 * Nécessite un JWT valide.
 */
@RestController
@RequestMapping("/api/share")
@CrossOrigin(
        originPatterns = {"http://localhost:4200", "http://127.0.0.1:4200"},
        allowCredentials = "true"
)
public class ShareController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShareController.class);

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    public ResponseEntity<ShareResponse> share(
            @RequestBody ShareRequest request,
            Authentication auth) {

        if (auth == null || auth.getName() == null) {
            log.warn("Tentative de partage sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(auth.getName());
        log.info("Demande de partage par {} — fileId={}, email={}",
                userId, request != null ? request.fileId() : null,
                request != null ? request.recipientEmail() : null);

        ShareResponse response = shareService.share(request, userId);
        return ResponseEntity.ok(response);
    }
}
