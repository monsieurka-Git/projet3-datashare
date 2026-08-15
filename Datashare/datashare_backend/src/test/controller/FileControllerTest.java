package com.datashare.backend.controller;

import com.datashare.backend.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    void uploadFile_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "Hello".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .header("Authorization", "Bearer fakeToken"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFile_shouldReturn403IfNotOwner() throws Exception {
        UUID fileId = UUID.randomUUID();

        Mockito.doThrow(new RuntimeException("Vous ne pouvez supprimer que vos propres fichiers"))
                .when(fileService).deleteFileForUser(Mockito.eq(fileId), Mockito.any());

        mockMvc.perform(delete("/api/files/info/" + fileId)
                        .header("Authorization", "Bearer fakeToken"))
                .andExpect(status().isForbidden());
    }
}
