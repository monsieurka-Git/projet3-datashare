package com.datashare.backend.service;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires FileDownloadService — US02
 */
@ExtendWith(MockitoExtension.class)
class FileDownloadServiceTest {

    @Mock private FileRepository fileRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private FileDownloadService fileDownloadService;

    private FileEntity file;
    private final String token = "tok-123";

    @BeforeEach
    void setUp() {
        file = new FileEntity();
        file.setId(UUID.randomUUID());
        file.setDownloadToken(token);
        file.setOriginalName("doc.pdf");
        file.setFilename("uuid_doc.pdf");
        file.setSize(1024L);
        file.setExpiresAt(Instant.now().plus(3, ChronoUnit.DAYS));
        file.setDownloadPasswordHash(null);
    }

    @Test
    void getFileMetadata_returnsValidFile() {
        when(fileRepository.findByDownloadToken(token)).thenReturn(Optional.of(file));

        FileEntity result = fileDownloadService.getFileMetadata(token);

        assertEquals("doc.pdf", result.getOriginalName());
        assertEquals(token, result.getDownloadToken());
    }

    @Test
    void getFileMetadata_rejectsExpiredLink() {
        file.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(fileRepository.findByDownloadToken(token)).thenReturn(Optional.of(file));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileDownloadService.getFileMetadata(token));
        assertTrue(ex.getMessage().toLowerCase().contains("expir"));
    }

    @Test
    void getFileMetadata_rejectsInvalidToken() {
        when(fileRepository.findByDownloadToken("bad")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileDownloadService.getFileMetadata("bad"));
        assertTrue(ex.getMessage().toLowerCase().contains("invalide")
                || ex.getMessage().toLowerCase().contains("introuvable"));
    }

    @Test
    void getFileForDownload_acceptsCorrectPassword() {
        file.setDownloadPasswordHash("hash");
        when(fileRepository.findByDownloadToken(token)).thenReturn(Optional.of(file));
        when(passwordEncoder.matches("secret1", "hash")).thenReturn(true);

        FileEntity result = fileDownloadService.getFileForDownload(token, "secret1");
        assertNotNull(result);
        assertEquals(token, result.getDownloadToken());
    }

    @Test
    void getFileForDownload_rejectsWrongPassword() {
        file.setDownloadPasswordHash("hash");
        when(fileRepository.findByDownloadToken(token)).thenReturn(Optional.of(file));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileDownloadService.getFileForDownload(token, "wrong"));
        assertTrue(ex.getMessage().toLowerCase().contains("incorrect")
                || ex.getMessage().toLowerCase().contains("mot de passe"));
    }

    @Test
    void getFileForDownload_requiresPasswordWhenProtected() {
        file.setDownloadPasswordHash("hash");
        when(fileRepository.findByDownloadToken(token)).thenReturn(Optional.of(file));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileDownloadService.getFileForDownload(token, null));
        assertTrue(ex.getMessage().toLowerCase().contains("requis")
                || ex.getMessage().toLowerCase().contains("mot de passe"));
    }

    @Test
    void getFileForDownload_allowsPublicFileWithoutPassword() {
        file.setDownloadPasswordHash(null);
        when(fileRepository.findByDownloadToken(token)).thenReturn(Optional.of(file));

        FileEntity result = fileDownloadService.getFileForDownload(token, null);
        assertNotNull(result);
    }
}
