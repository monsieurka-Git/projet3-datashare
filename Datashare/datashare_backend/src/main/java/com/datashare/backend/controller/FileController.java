package com.datashare.backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datashare.backend.dto.FileDto;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/{id}")
    public FileDto getFile(@PathVariable UUID id) {
        FileEntity file = fileService.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        return new FileDto(file.getId(), file.getFilename(), file.getDownloadLink(), file.getOwnerId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        fileService.delete(id);
    }
}