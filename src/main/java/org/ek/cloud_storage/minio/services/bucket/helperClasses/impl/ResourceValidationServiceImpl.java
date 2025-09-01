package org.ek.cloud_storage.minio.services.bucket.helperClasses.impl;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.exception.MinIoResourceErrorException;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceValidationService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceValidationServiceImpl implements ResourceValidationService {

    private final MinioClient minioClient;

    private final MinioConfig  minioConfig;
    @Override
    public void validateResourceExists(String path) throws IOException {
        if (!resourceExists(path)) {
            String resourceType = path.endsWith("/") ? "Folder" : "File";
            throw new MinIoResourceErrorException(resourceType + " does not exist: " + path);
        }
    }

    @Override
    public boolean booleanValidateResourceExists(String path) {
        try {

            if (path.endsWith("/")) {
                return folderExists(path);
            }

            else {
                return fileExists(path);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a resource exists (file or folder)
     */
    @Override
    public boolean resourceExists(String path) {
        try {
            return path.endsWith("/") ? folderExists(path) : fileExists(path);
        } catch (IOException e) {
            log.error("Error checking resource existence: {}", path, e);
            return false;
        }
    }

    /**
     * Checks if a file exists at the specified path
     */
    @Override
    public boolean fileExists(String path) throws IOException {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new IOException("MinIO error checking file existence: " + path, e);
        } catch (Exception e) {
            throw new IOException("Unexpected error checking file existence: " + path, e);
        }
    }

    /**
     * Checks if a folder exists at the specified path
     */
    @Override
    public boolean folderExists(String path) throws IOException {
        if ("/".equals(path)) {
            return true;
        }

        String normalizedPath = path.endsWith("/") ? path : path + "/";

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .prefix(normalizedPath)
                            .maxKeys(1)
                            .build());

            return results.iterator().hasNext();
        } catch (Exception e) {
            throw new IOException("Error checking folder existence: " + path, e);
        }
    }

    /**
     * Determines if a path represents a folder
     */
    @Override
    public boolean isFolder(String path) {
        String prefix = path.endsWith("/") ? path : path + "/";

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .prefix(prefix)
                            .maxKeys(1)
                            .build());

            return results.iterator().hasNext();
        } catch (Exception e) {
            log.error("Error determining if path is folder: {}", path, e);
            return false;
        }
    }
}

