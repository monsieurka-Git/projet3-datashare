package com.datashare.backend.controller;

import com.datashare.backend.dto.FileMetadataResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileDownloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDownloadControllerTest {

    @Mock private FileDownloadService fileDownloadService;
    @InjectMocks private FileDownloadController controller;

    @Test
    void getMetadata_returnsResponse() {
        FileEntity f = new FileEntity();
        f.setOriginalName("doc.pdf");
        f.setSize(100L);
        f.setContentType("application/pdf");
        f.setExpiresAt(Instant.now().plusSeconds(86400));
        f.setDownloadPasswordHash(null);
        when(fileDownloadService.getFileMetadata("tok")).thenReturn(f);

        ResponseEntity<FileMetadataResponse> res = controller.getMetadata("tok");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("doc.pdf", res.getBody().getOriginalName());
        assertFalse(res.getBody().isPasswordProtected());
    }

    @Test
    void download_returnsBytes() throws Exception {
        FileEntity f = new FileEntity();
        f.setFilename("stored.txt");
        f.setOriginalName("hello.txt");
        f.setContentType("text/plain");
        when(fileDownloadService.getFileForDownload("tok", null)).thenReturn(f);
        when(fileDownloadService.loadFileBytes("stored.txt")).thenReturn("hello".getBytes());

        ResponseEntity<byte[]> res = controller.download("tok", null);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertArrayEquals("hello".getBytes(), res.getBody());
        assertTrue(res.getHeaders().getFirst("Content-Disposition").contains("hello.txt"));
    }
}
