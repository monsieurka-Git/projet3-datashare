package com.datashare.backend.service;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    FileServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadFile_shouldSaveFileAndReturnEntity() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "Hello".getBytes()
        );
        UUID userId = UUID.randomUUID();

        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileEntity saved = fileService.uploadFile(file, userId);

        assertNotNull(saved.getId());
        assertEquals("application/pdf", saved.getContentType());
        assertEquals(userId, saved.getOwnerId());
    }

    @Test
    void deleteFileForUser_shouldThrowIfNotOwner() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        FileEntity file = new FileEntity();
        file.setId(fileId);
        file.setOwnerId(UUID.randomUUID());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.deleteFileForUser(fileId, userId));

        assertEquals("Vous ne pouvez supprimer que vos propres fichiers", ex.getMessage());
    }

    @Test
    void deleteFileForUser_shouldThrowIfFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.deleteFileForUser(fileId, userId));

        assertTrue(ex.getMessage().contains("introuvable"));
    }
}
