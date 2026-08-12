package com.datashare.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité JPA représentant un fichier uploadé sur DataShare.
 */
@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    private String originalName;

    private Long size;

    private String contentType;

    @Column(nullable = false)
    private String storagePath;

    @Column(unique = true)
    private String downloadToken;

    private String downloadPasswordHash;

    private Instant expiresAt;

    private Instant createdAt = Instant.now();

    private UUID ownerId;

    private String tags;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getDownloadToken() { return downloadToken; }
    public void setDownloadToken(String downloadToken) { this.downloadToken = downloadToken; }

    public String getDownloadPasswordHash() { return downloadPasswordHash; }
    public void setDownloadPasswordHash(String downloadPasswordHash) { this.downloadPasswordHash = downloadPasswordHash; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
