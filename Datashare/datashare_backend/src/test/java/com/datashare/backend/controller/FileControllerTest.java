package com.datashare.backend.controller;

import com.datashare.backend.dto.UploadDto;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock private FileService fileService;
    @Mock private MultipartFile multipartFile;
    @InjectMocks private FileController fileController;

    private Authentication auth(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null);
    }

    @Test
    void upload_requiresAuth() throws Exception {
        ResponseEntity<UploadDto> res = fileController.upload(multipartFile, 7, null, null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
    }

    @Test
    void upload_authenticated_returnsDto() throws Exception {
        UUID userId = UUID.randomUUID();
        FileEntity saved = new FileEntity();
        saved.setId(UUID.randomUUID());
        saved.setFilename("f");
        saved.setOriginalName("o.txt");
        saved.setDownloadToken("tok");
        when(fileService.uploadFile(any(), eq(userId), any(), any(), any())).thenReturn(saved);
        when(multipartFile.getOriginalFilename()).thenReturn("o.txt");

        ResponseEntity<UploadDto> res = fileController.upload(multipartFile, 7, null, null, auth(userId));
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("tok", res.getBody().downloadToken());
    }

    @Test
    void uploadAnonymous_returnsDto() throws Exception {
        FileEntity saved = new FileEntity();
        saved.setId(UUID.randomUUID());
        saved.setFilename("f");
        saved.setOriginalName("a.txt");
        saved.setDownloadToken("anon-tok");
        when(fileService.uploadFile(any(), isNull(), any(), any(), isNull())).thenReturn(saved);
        when(multipartFile.getOriginalFilename()).thenReturn("a.txt");

        ResponseEntity<UploadDto> res = fileController.uploadAnonymous(multipartFile, 5, null);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("anon-tok", res.getBody().downloadToken());
    }

    @Test
    void getUserFiles_requiresAuth() {
        assertEquals(HttpStatus.UNAUTHORIZED, fileController.getUserFiles(null).getStatusCode());
    }

    @Test
    void getUserFiles_returnsList() {
        UUID userId = UUID.randomUUID();
        when(fileService.findByUser(userId)).thenReturn(List.of(new FileEntity()));
        ResponseEntity<?> res = fileController.getUserFiles(auth(userId));
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void delete_requiresAuth() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                fileController.delete(UUID.randomUUID(), null).getStatusCode());
    }

    @Test
    void delete_callsService() {
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        doNothing().when(fileService).deleteFileForUser(fileId, userId);

        ResponseEntity<Void> res = fileController.delete(fileId, auth(userId));
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        verify(fileService).deleteFileForUser(fileId, userId);
    }

    @Test
    void getFile_returnsDto() {
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        FileEntity f = new FileEntity();
        f.setId(fileId);
        f.setFilename("n");
        f.setDownloadToken("t");
        f.setOwnerId(userId);
        when(fileService.findById(fileId)).thenReturn(Optional.of(f));

        ResponseEntity<?> res = fileController.getFile(fileId, auth(userId));
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void updateTags_requiresAuth() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                fileController.updateTags(UUID.randomUUID(), "a,b", null).getStatusCode());
    }

    @Test
    void updateTags_ok() {
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        FileEntity f = new FileEntity();
        f.setId(fileId);
        f.setTags("a,b");
        when(fileService.updateTags(fileId, userId, "a,b")).thenReturn(f);

        ResponseEntity<FileEntity> res = fileController.updateTags(fileId, "a,b", auth(userId));
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("a,b", res.getBody().getTags());
    }
}
