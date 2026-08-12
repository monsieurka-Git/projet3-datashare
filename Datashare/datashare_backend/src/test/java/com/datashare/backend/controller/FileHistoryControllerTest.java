package com.datashare.backend.controller;

import com.datashare.backend.dto.FileHistoryResponse;
import com.datashare.backend.service.FileHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileHistoryControllerTest {

    @Mock private FileHistoryService fileHistoryService;
    @InjectMocks private FileHistoryController controller;

    @Test
    void getHistory_unauthorizedWithoutAuth() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getHistory(null).getStatusCode());
    }

    @Test
    void getHistory_returnsList() {
        UUID userId = UUID.randomUUID();
        when(fileHistoryService.getUserHistory(userId)).thenReturn(List.of(
                FileHistoryResponse.builder().originalName("a.txt").size(1).expired(false).build()
        ));
        var auth = new UsernamePasswordAuthenticationToken(userId.toString(), null);
        ResponseEntity<List<FileHistoryResponse>> res = controller.getHistory(auth);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }
}
