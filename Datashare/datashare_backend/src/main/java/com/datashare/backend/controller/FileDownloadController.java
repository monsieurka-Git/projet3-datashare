package com.datashare.backend.controller;

import com.datashare.backend.dto.FileMetadataResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    /**
     * US02 — Affichage des métadonnées avant téléchargement.
     * Accessible à tout utilisateur.
     */
    @GetMapping("/metadata/{token}")
    public ResponseEntity<FileMetadataResponse> getMetadata(@PathVariable String token) {

        FileEntity file = fileDownloadService.getFileForDownload(token, null);

        FileMetadataResponse response = FileMetadataResponse.builder()
                .originalName(file.getOriginalName())
                .size(file.getSize())
                .contentType(file.getContentType())
                .expiresAt(file.getExpiresAt())
                .passwordProtected(file.getDownloadPasswordHash() != null)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * US02 — Téléchargement du fichier via token.
     * Mot de passe requis si protégé.
     */
    @PostMapping("/download/{token}")
    public ResponseEntity<byte[]> download(
            @PathVariable String token,
            @RequestParam(value = "password", required = false) String password
    ) throws Exception {

        FileEntity file = fileDownloadService.getFileForDownload(token, password);

        byte[] data = fileDownloadService.loadFileBytes(file.getFilename());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(data);
    }
}
