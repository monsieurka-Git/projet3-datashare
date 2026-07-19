package com.datashare.backend.dto;

import java.util.UUID;

public record FileDto(
    UUID id,
    String filename,
    String downloadLink,
    UUID ownerId
) {}