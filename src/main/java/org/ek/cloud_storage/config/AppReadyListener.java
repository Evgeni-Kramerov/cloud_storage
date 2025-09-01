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
        int retries = 10;
        int delaySeconds = 3;

        for (int i = 0; i < retries; i++) {
            try {
                bucketService.createBucketIfNotExists();
                System.out.println("Bucket checked/created successfully.");
                return;  // success, exit method
            } catch (IOException e) {
                System.err.println("Attempt " + (i + 1) + " failed: " + e.getMessage());
                if (i == retries - 1) {
                    throw new RuntimeException("Failed to connect to MinIO after retries", e);
                }
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for MinIO", ie);
                }
            }
        }
    }
}

