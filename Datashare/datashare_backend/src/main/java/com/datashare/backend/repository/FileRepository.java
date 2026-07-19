package com.datashare.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.datashare.backend.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    List<FileEntity> findByOwnerId(UUID ownerId);
    Optional<FileEntity> findByDownloadLink(String link);
}