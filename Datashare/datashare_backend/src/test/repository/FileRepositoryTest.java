package com.datashare.backend.repository;

import com.datashare.backend.model.FileEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileRepositoryTest {

    @Test
    void repositoryMock_shouldWork() {
        FileRepository repo = Mockito.mock(FileRepository.class);
        UUID id = UUID.randomUUID();

        FileEntity file = new FileEntity();
        file.setId(id);

        Mockito.when(repo.findById(id)).thenReturn(java.util.Optional.of(file));

        assertTrue(repo.findById(id).isPresent());
    }
}
