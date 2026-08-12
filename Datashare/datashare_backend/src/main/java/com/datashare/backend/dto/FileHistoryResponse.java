package com.datashare.backend.dto;

import java.time.Instant;

/**
 * DTO historique des fichiers (US05).
 */
public class FileHistoryResponse {

    private String originalName;
    private long size;
    private Instant uploadedAt;
    private Instant expiresAt;
    private boolean expired;

    public FileHistoryResponse() {}

    public FileHistoryResponse(String originalName, long size, Instant uploadedAt, Instant expiresAt, boolean expired) {
        this.originalName = originalName;
        this.size = size;
        this.uploadedAt = uploadedAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String originalName;
        private long size;
        private Instant uploadedAt;
        private Instant expiresAt;
        private boolean expired;

        public Builder originalName(String v) { this.originalName = v; return this; }
        public Builder size(long v) { this.size = v; return this; }
        public Builder uploadedAt(Instant v) { this.uploadedAt = v; return this; }
        public Builder expiresAt(Instant v) { this.expiresAt = v; return this; }
        public Builder expired(boolean v) { this.expired = v; return this; }

        public FileHistoryResponse build() {
            return new FileHistoryResponse(originalName, size, uploadedAt, expiresAt, expired);
        }
    }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
}
