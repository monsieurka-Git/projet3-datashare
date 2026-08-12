package com.datashare.backend.dto;

import java.util.UUID;

public record UploadDto(
    UUID id,
    String filename,
    String originalName,
    String downloadToken,
    String downloadUrl
) {}
