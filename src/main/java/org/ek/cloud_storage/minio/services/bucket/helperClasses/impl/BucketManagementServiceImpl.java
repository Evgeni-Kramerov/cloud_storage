package org.ek.cloud_storage.minio.services.bucket.helperClasses.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.BucketManagementService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class BucketManagementServiceImpl implements BucketManagementService {

    private final MinioClient minioClient;

    private final MinioConfig minioConfig;

    /**
     * Create bucket if not exists for users with name specified in MinIO config
     * @throws IOException
     */
    @Override
    public void createBucketIfNotExists() throws IOException {
        try {
            if (bucketExists())
                return;

            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build());
        }   catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                   InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if bucket specified in MinIO config exists
     * @return
     * @throws IOException
     */
    private boolean bucketExists() throws IOException {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs
                            .builder()
                            .bucket(minioConfig.getBucketName())
                            .build());

        }    catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                    InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                    XmlParserException e) {
            throw new IOException("Failed to check if bucket exists " + e);
        }
    }
}
