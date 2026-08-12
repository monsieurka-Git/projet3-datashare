package com.datashare.backend.dto;

import java.time.Instant;

public class FileMetadataResponse {
    private String originalName;
    private long size;
    private String contentType;
    private Instant expiresAt;
    private boolean passwordProtected;

    public FileMetadataResponse() {}

    public FileMetadataResponse(String originalName, long size, String contentType, Instant expiresAt, boolean passwordProtected) {
        this.originalName = originalName;
        this.size = size;
        this.contentType = contentType;
        this.expiresAt = expiresAt;
        this.passwordProtected = passwordProtected;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String originalName;
        private long size;
        private String contentType;
        private Instant expiresAt;
        private boolean passwordProtected;
        public Builder originalName(String v) { this.originalName = v; return this; }
        public Builder size(long v) { this.size = v; return this; }
        public Builder contentType(String v) { this.contentType = v; return this; }
        public Builder expiresAt(Instant v) { this.expiresAt = v; return this; }
        public Builder passwordProtected(boolean v) { this.passwordProtected = v; return this; }
        public FileMetadataResponse build() {
            return new FileMetadataResponse(originalName, size, contentType, expiresAt, passwordProtected);
        }
    }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isPasswordProtected() { return passwordProtected; }
    public void setPasswordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; }
}
