package com.datashare.backend.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.datashare.backend.dto.FileDto;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * ✅ POST /api/files/upload
     * Upload d'un fichier. Nécessite un token JWT valide.
     * 
     * Test avec Postman :
     * - Méthode : POST
     * - URL : http://localhost:8080/api/files/upload
     * - Header : Authorization: Bearer <token_JWT>
     * - Body : form-data
     *   - Key : "file" (type File)
     *   - Value : sélectionner le fichier
     * 
     * Test avec Swagger :
     * - Cliquer sur "Authorize" et coller le token JWT
     * - Utiliser l'endpoint POST /api/files/upload
     */
    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileEntity> upload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {

        log.debug("📥 Requête d'upload reçue : {}", file.getOriginalFilename());

        // ✅ Vérifie que l'utilisateur est authentifié (sécurité supplémentaire)
        if (auth == null || auth.getName() == null) {
            log.warn("⚠️  Tentative d'upload sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ Récupère l'UUID de l'utilisateur depuis l'authentification
        // (auth.getName() = UUID positionné par JwtFilter)
        UUID userId = UUID.fromString(auth.getName());
        log.debug("👤 Upload par l'utilisateur : {}", userId);

        // ✅ Upload du fichier via le service
        FileEntity saved = fileService.uploadFile(file, userId);

        log.debug("✅ Upload réussi, ID du fichier : {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public FileDto getFile(@PathVariable UUID id) {
        FileEntity file = fileService.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        return new FileDto(file.getId(), file.getFilename(), file.getDownloadToken(), file.getOwnerId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        fileService.delete(id);
    }
}
