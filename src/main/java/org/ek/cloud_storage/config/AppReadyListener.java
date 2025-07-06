package org.ek.cloud_storage.config;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.services.BucketService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AppReadyListener {

    private final BucketService bucketService;

    //TODO Move to properties
    private final String bucketName = "user-files";

    @EventListener(ApplicationReadyEvent.class)
    public void appReady() {
        try {
            if (!bucketService.bucketExists(bucketName)){
                System.out.println("Bucket does not exist");
                bucketService.createBucket(bucketName);
                System.out.println("Bucket " + bucketName +" created");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
