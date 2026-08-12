package com.datashare.backend.service;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires FileService — US01, US05, US06
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock private FileRepository fileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MultipartFile multipartFile;

    @InjectMocks private FileService fileService;

    @TempDir Path tempDir;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        // Redirige le stockage vers un dossier temporaire isolé
        ReflectionTestUtils.setField(fileService, "storagePath", tempDir);
    }

    @Test
    void uploadFile_generatesUniqueTokenAndAssociatesOwner() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("rapport.pdf");
        when(multipartFile.getSize()).thenReturn(2048L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(inv -> {
            FileEntity e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        FileEntity saved = fileService.uploadFile(multipartFile, userId, 7, null, null);

        assertNotNull(saved.getDownloadToken());
        assertFalse(saved.getDownloadToken().isBlank());
        assertEquals(userId, saved.getOwnerId());
        assertEquals("rapport.pdf", saved.getOriginalName());
        assertNotNull(saved.getExpiresAt());
    }

    @Test
    void uploadFile_hashesPasswordWhenProvided() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("secret.txt");
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("x".getBytes()));
        when(passwordEncoder.encode("secret1")).thenReturn("bcrypt-hash");
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        FileEntity saved = fileService.uploadFile(multipartFile, userId, 3, "secret1", null);

        assertEquals("bcrypt-hash", saved.getDownloadPasswordHash());
        verify(passwordEncoder).encode("secret1");
    }

    @Test
    void uploadFile_rejectsForbiddenExtension() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("malware.exe");
        when(multipartFile.getSize()).thenReturn(100L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadFile(multipartFile, userId, 7, null, null));
        assertTrue(ex.getMessage().toLowerCase().contains("interdit"));
        verify(fileRepository, never()).save(any());
    }

    @Test
    void uploadFile_rejectsOversizedFile() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("big.bin");
        when(multipartFile.getSize()).thenReturn(2L * 1024 * 1024 * 1024); // 2 Go

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.uploadFile(multipartFile, userId, 7, null, null));
        assertTrue(ex.getMessage().toLowerCase().contains("taille")
                || ex.getMessage().toLowerCase().contains("1 go"));
    }

    @Test
    void uploadFile_anonymousHasNullOwner() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("anon.txt");
        when(multipartFile.getSize()).thenReturn(50L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("a".getBytes()));
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        FileEntity saved = fileService.uploadFile(multipartFile, null, 7, null, null);
        assertNull(saved.getOwnerId());
        assertNotNull(saved.getDownloadToken());
    }

    @Test
    void findByUser_returnsOnlyOwnerFiles() {
        FileEntity f = new FileEntity();
        f.setOwnerId(userId);
        when(fileRepository.findByOwnerId(userId)).thenReturn(List.of(f));

        List<FileEntity> result = fileService.findByUser(userId);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getOwnerId());
    }

    @Test
    void deleteFileForUser_deletesOwnFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        Path physical = tempDir.resolve("stored.txt");
        java.nio.file.Files.writeString(physical, "data");

        FileEntity entity = new FileEntity();
        entity.setId(fileId);
        entity.setOwnerId(userId);
        entity.setStoragePath(physical.toString());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(entity));

        fileService.deleteFileForUser(fileId, userId);

        verify(fileRepository).delete(entity);
        assertFalse(java.nio.file.Files.exists(physical));
    }

    @Test
    void deleteFileForUser_rejectsOtherUsersFile() {
        UUID fileId = UUID.randomUUID();
        FileEntity entity = new FileEntity();
        entity.setId(fileId);
        entity.setOwnerId(UUID.randomUUID()); // autre propriétaire
        entity.setStoragePath(tempDir.resolve("x.txt").toString());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(entity));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.deleteFileForUser(fileId, userId));
        assertTrue(ex.getMessage().toLowerCase().contains("propres")
                || ex.getMessage().toLowerCase().contains("supprimer"));
        verify(fileRepository, never()).delete(any(FileEntity.class));
    }

    @Test
    void normalizeTags_enforcesMaxLengthAndNoDuplicates() {
        String tags = fileService.normalizeTags("travail, Travail, urgent");
        assertEquals("travail,urgent", tags);

        assertThrows(RuntimeException.class,
                () -> fileService.normalizeTags("a".repeat(31)));
    }

    @Test
    void purgeExpiredFiles_removesExpiredEntries() throws Exception {
        Path physical = tempDir.resolve("old.txt");
        java.nio.file.Files.writeString(physical, "old");

        FileEntity expired = new FileEntity();
        expired.setId(UUID.randomUUID());
        expired.setOriginalName("old.txt");
        expired.setStoragePath(physical.toString());
        expired.setExpiresAt(java.time.Instant.now().minusSeconds(3600));

        when(fileRepository.findByExpiresAtBefore(any())).thenReturn(List.of(expired));

        int count = fileService.purgeExpiredFiles();
        assertEquals(1, count);
        verify(fileRepository).delete(expired);
        assertFalse(java.nio.file.Files.exists(physical));
    }
}
