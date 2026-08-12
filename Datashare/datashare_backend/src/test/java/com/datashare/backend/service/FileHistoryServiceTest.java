package com.datashare.backend.service;

import com.datashare.backend.dto.FileHistoryResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires FileHistoryService — US05
 */
@ExtendWith(MockitoExtension.class)
class FileHistoryServiceTest {

    @Mock private FileRepository fileRepository;
    @InjectMocks private FileHistoryService fileHistoryService;

    @Test
    void getUserHistory_returnsCompleteHistory() {
        UUID userId = UUID.randomUUID();

        FileEntity active = new FileEntity();
        active.setOriginalName("a.txt");
        active.setSize(100L);
        active.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        active.setExpiresAt(Instant.now().plus(5, ChronoUnit.DAYS));

        FileEntity expired = new FileEntity();
        expired.setOriginalName("b.txt");
        expired.setSize(200L);
        expired.setCreatedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        expired.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));

        when(fileRepository.findByOwnerId(userId)).thenReturn(List.of(active, expired));

        List<FileHistoryResponse> history = fileHistoryService.getUserHistory(userId);

        assertEquals(2, history.size());
    }

    @Test
    void getUserHistory_marksExpiredStatusCorrectly() {
        UUID userId = UUID.randomUUID();

        FileEntity active = new FileEntity();
        active.setOriginalName("valid.pdf");
        active.setSize(50L);
        active.setCreatedAt(Instant.now());
        active.setExpiresAt(Instant.now().plus(2, ChronoUnit.DAYS));

        FileEntity expired = new FileEntity();
        expired.setOriginalName("old.pdf");
        expired.setSize(50L);
        expired.setCreatedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        expired.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(fileRepository.findByOwnerId(userId)).thenReturn(List.of(active, expired));

        List<FileHistoryResponse> history = fileHistoryService.getUserHistory(userId);

        FileHistoryResponse validItem = history.stream()
                .filter(h -> "valid.pdf".equals(h.getOriginalName()))
                .findFirst().orElseThrow();
        FileHistoryResponse expiredItem = history.stream()
                .filter(h -> "old.pdf".equals(h.getOriginalName()))
                .findFirst().orElseThrow();

        assertFalse(validItem.isExpired());
        assertTrue(expiredItem.isExpired());
    }

    @Test
    void getUserHistory_returnsEmptyListWhenNoFiles() {
        UUID userId = UUID.randomUUID();
        when(fileRepository.findByOwnerId(userId)).thenReturn(List.of());

        assertTrue(fileHistoryService.getUserHistory(userId).isEmpty());
    }
}
