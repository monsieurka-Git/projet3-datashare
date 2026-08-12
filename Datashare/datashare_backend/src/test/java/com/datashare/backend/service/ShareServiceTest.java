package com.datashare.backend.service;

import com.datashare.backend.dto.ShareRequest;
import com.datashare.backend.dto.ShareResponse;
import com.datashare.backend.model.FileEntity;
import com.datashare.backend.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock private FileRepository fileRepository;
    @InjectMocks private ShareService shareService;

    @Test
    void share_success() {
        UUID ownerId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        FileEntity file = new FileEntity();
        file.setId(fileId);
        file.setOwnerId(ownerId);
        file.setOriginalName("doc.pdf");
        file.setDownloadToken("tok");
        file.setExpiresAt(Instant.now().plus(2, ChronoUnit.DAYS));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        ShareRequest req = new ShareRequest(fileId.toString(), "dest@test.com");
        ShareResponse res = shareService.share(req, ownerId);

        assertNotNull(res);
        assertTrue(res.downloadUrl().contains("tok") || res.message() != null);
    }

    @Test
    void share_rejectsMissingEmail() {
        assertThrows(RuntimeException.class,
                () -> shareService.share(new ShareRequest(UUID.randomUUID().toString(), ""), UUID.randomUUID()));
    }

    @Test
    void share_rejectsOtherOwner() {
        UUID fileId = UUID.randomUUID();
        FileEntity file = new FileEntity();
        file.setId(fileId);
        file.setOwnerId(UUID.randomUUID());
        file.setDownloadToken("tok");
        file.setExpiresAt(Instant.now().plusSeconds(3600));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertThrows(RuntimeException.class,
                () -> shareService.share(new ShareRequest(fileId.toString(), "a@b.com"), UUID.randomUUID()));
    }
}
