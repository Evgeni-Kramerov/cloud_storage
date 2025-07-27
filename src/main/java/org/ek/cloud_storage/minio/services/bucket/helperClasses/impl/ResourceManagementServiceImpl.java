package org.ek.cloud_storage.minio.services.bucket.helperClasses.impl;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.exception.MinIoResourceErrorException;
import org.ek.cloud_storage.minio.services.PathService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceInfoService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceManagementService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceValidationService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceManagementServiceImpl implements ResourceManagementService {

    private final MinioClient minioClient;

    private final MinioConfig  minioConfig;

    private final ResourceValidationService resourceValidationService;
    private final ResourceInfoService resourceInfoService;
    private final PathService pathService;

    @Override
    public void moveResource(String oldPath, String newPath) throws IOException {
        resourceValidationService.validateResourceExists(oldPath);

        if (resourceValidationService.booleanValidateResourceExists(newPath)) {
            throw new MinIoResourceErrorException("Resource already exists");
        }

        if (resourceValidationService.isFolder(oldPath)) {
            System.out.println("Copying folder + " + oldPath);
            copyFolder(oldPath,newPath);
        }

        else {
            copyFile(oldPath, newPath);
        }
    }

    private void copyFile(String oldPath, String newPath) throws IOException {

        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(newPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(minioConfig.getBucketName())
                                            .object(oldPath)
                                            .build())
                            .build());

            deleteFile(oldPath);
        }
        catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
               InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyFolder(String oldPath, String newPath) {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(oldPath)
                        .recursive(true)
                        .build()
        );

        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                String relativePath = item.objectName().substring(oldPath.length());
                System.out.println("Relative path - " + relativePath);
                copyFile(oldPath + relativePath, newPath + relativePath);
            }

        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    @Override
    public void deleteResource(String path) throws IOException {
        resourceValidationService.validateResourceExists(path);


        if (resourceValidationService.isFolder(path)){
            deleteFolder(path);
        }

        else {
            deleteFile(path);
        }

    }

    private void deleteFolder(String path) throws IOException {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(path)
                        .recursive(true)
                        .build()
        );

        try {
            List<DeleteObject> objectsToDelete = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                objectsToDelete.add(new DeleteObject(item.objectName()));
            }

            if (!objectsToDelete.isEmpty()) {
                Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .objects(objectsToDelete)
                                .build()
                );

                for (Result<DeleteError> error : errors) {
                    try {
                        DeleteError err = error.get();
                        System.err.println("Failed to delete object: " + err.objectName() + " - " + err.message());
                    } catch (Exception e) {
                        System.err.println("Error during deletion result: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete folder: " + path, e);
        }
    }

    private void deleteFile(String path) throws IOException {
        try {

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource createEmptyFolder(String path) throws IOException {
        if (resourceValidationService.folderExists(path)) {
            throw new MinIoResourceErrorException("Folder already exists");
        }


        if (!resourceValidationService.folderExists(pathService.getParentFolderPath(path))) {

            throw new MinIoResourceErrorException("Parent folder doesnt exists");
        }


        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .stream(emptyStream, 0, -1)
                    .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
        return resourceInfoService.getResourceInfo(path);
    }
}

