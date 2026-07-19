package com.datashare.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public FileEntity save(FileEntity file) {
        return fileRepository.save(file);
    }

    public Optional<FileEntity> findById(UUID id) {
        return fileRepository.findById(id);
    }

    public Optional<FileEntity> findByLink(String link) {
        return fileRepository.findByDownloadLink(link);
    }

    public List<FileEntity> findByUser(UUID userId) {
        return fileRepository.findByOwnerId(userId);
    }

    public void delete(UUID id) {
        fileRepository.deleteById(id);
    }
}