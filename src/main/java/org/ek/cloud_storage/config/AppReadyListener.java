package org.ek.cloud_storage.config;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.services.bucket.BucketService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AppReadyListener {

    private final BucketService bucketService;

    @EventListener(ApplicationReadyEvent.class)
    public void appReady() {
        try {
                bucketService.createBucketIfNotExists();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
