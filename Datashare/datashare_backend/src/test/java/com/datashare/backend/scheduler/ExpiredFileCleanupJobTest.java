package com.datashare.backend.scheduler;

import com.datashare.backend.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredFileCleanupJobTest {

    @Mock private FileService fileService;
    @InjectMocks private ExpiredFileCleanupJob job;

    @Test
    void purgeExpiredFiles_callsService() {
        when(fileService.purgeExpiredFiles()).thenReturn(3);
        job.purgeExpiredFiles();
        verify(fileService).purgeExpiredFiles();
    }

    @Test
    void purgeExpiredFiles_handlesException() {
        when(fileService.purgeExpiredFiles()).thenThrow(new RuntimeException("disk error"));
        // ne doit pas propager
        job.purgeExpiredFiles();
        verify(fileService).purgeExpiredFiles();
    }
}
