package com.datashare.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
public class FileEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String filename;

    private Long size;

    private String contentType;

    @Column(nullable = false)
    private String storagePath;

    @Column(unique = true)
    private String downloadLink;

    private String passwordHash;

    private LocalDate expirationDate;

    private Instant createdAt = Instant.now();

    private UUID ownerId;

    private String tags;
}
