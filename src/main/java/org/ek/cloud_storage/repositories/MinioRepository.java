package org.ek.cloud_storage.repositories;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.config.MinioConfig;
import org.ek.cloud_storage.domain.resource.DownloadResource;
import org.ek.cloud_storage.domain.resource.FileResource;
import org.ek.cloud_storage.domain.resource.FolderResource;
import org.ek.cloud_storage.exception.MinIoResourceException;
import org.ek.cloud_storage.services.PathService;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MinioRepository {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    
    private final PathService pathService;

    public void saveFile(MultipartFile file, String name) {
        log.debug("Saving file {} to Minio", name);
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(name)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

        } catch (Exception e) {
            log.error("Error saving file {} to Minio", name, e);
            throw new MinIoResourceException("Failed to upload file " + name);
        }
    }

    public void creatEmptyFolder(String folder) {
        log.debug("Creating folder {} to Minio", folder);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(folder + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .contentType("application/x-directory")
                            .build()
            );
        } catch (Exception e) {
            log.error("Error creating folder {} to Minio", folder, e);
            throw new MinIoResourceException("Failed to create empty Folder " + folder);
        }
    }

    public DownloadResource downloadFolder(String path){

        log.debug("Downloading folder {} from Minio", path);

        StreamingResponseBody zipStream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                Iterable<Result<Item>> objects = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .prefix(path)
                                .recursive(true)
                                .build()
                );

                for (Result<Item> result : objects) {
                    Item item = result.get();

                    if (item.size() == 0 && item.objectName().endsWith("/")) continue;

                    String relativePath = item.objectName().substring(path.length());

                    try (InputStream stream = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(minioConfig.getBucketName())
                                    .object(item.objectName())
                                    .build()
                    )) {
                        zipOut.putNextEntry(new ZipEntry(relativePath));
                        stream.transferTo(zipOut);
                        zipOut.closeEntry();
                    }
                }
            } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                     InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
                log.error("Error downloading folder {} from Minio", path, e);
                throw new MinIoResourceException("Error downloading folder " + path);
            }
        };

        String zipFileName = Paths.get(path).getFileName().toString() + ".zip";

        return new DownloadResource(zipFileName, zipStream);
    }

    public DownloadResource downloadFile(String path) {
        log.debug("Downloading file {} from Minio", path);
        StreamingResponseBody stream = outputStream -> {

            try(InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(path)
                            .build()
            )) {
                inputStream.transferTo(outputStream);
            } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                     InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e){
                log.error("Error downloading file {} from Minio", path, e);
                throw new MinIoResourceException("Error downloading file " + path);
            }
        };

        String fileName = Paths.get(path).getFileName().toString();

        return new DownloadResource(fileName, stream);
    }
    public void deleteFolder(String path) {
        log.debug("Deleting folder {} from Minio", path);

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(path)
                        .recursive(true)
                        .build()
        );

        try {
            List<DeleteObject> objectsToDelete = new ArrayList<>();

            for (Result<Item> result2 : results) {
                Item item = result2.get();
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
            log.error("Error deleting folder {} from Minio", path, e);
            throw new MinIoResourceException("Failed to delete folder: " + path);
        }
    }
    public void deleteFile(String path) {
        log.debug("Deleting file {} from Minio", path);
        try {

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | IOException e) {
            throw new MinIoResourceException("Failed to delete file " + path);
        }
    }
    public FolderResource getFolder(String path){

        log.debug("Getting folder {} from Minio", path);

        if (!"/".equals(path)) {
            String normalizedPath = path.endsWith("/") ? path : path + "/";
            try {
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .prefix(normalizedPath)
                                .maxKeys(1)
                                .build());

                results.iterator();
            } catch (Exception e) {
                log.error("Error getting folder {} from Minio", path, e);
                throw new MinIoResourceException("Error getting folder : " + path);
            }
        }

        return new FolderResource(
                pathService.getFolderPath(path),
                pathService.getResourceName(path));
    }
    public FileResource getFile(String path) {
        log.debug("Getting file {} from Minio", path);
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(path)
                    .build());
        } catch (ErrorResponseException e1) {
            if (!"NoSuchKey".equals(e1.errorResponse().code())) {
                log.error("Error getting file {} from Minio", path, e1);
                throw new MinIoResourceException("MinIO error checking file existence: " + path);
            }
        } catch (Exception e1) {
            log.error("Error getting file {} from Minio", path, e1);
            throw new MinIoResourceException("MinIO error checking file existence: " + path);
        }

        Path pathObj = Paths.get(path);

        try {

            StatObjectResponse objectStat =
                    minioClient.statObject(StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(path)
                            .build());

            return new FileResource(
                    pathService.removeUserPrefixFromPath(path),
                    pathObj.getFileName().toString(),
                    objectStat.size()
            );

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | IOException e) {
            log.error("Error getting file {} from Minio", path, e);
            throw new MinIoResourceException("Error getting file : " + path);
        }

    }

    public void copyFolder(String pathFrom, String pathTo) {

        log.debug("Copying folder {} from Minio", pathFrom);

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(pathFrom)
                        .recursive(true)
                        .build()
        );

        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                String relativePath = item.objectName().substring(pathFrom.length());
                try {
                    minioClient.copyObject(
                            CopyObjectArgs.builder()
                                    .bucket(minioConfig.getBucketName())
                                    .object(pathTo + relativePath)
                                    .source(
                                            CopySource.builder()
                                                    .bucket(minioConfig.getBucketName())
                                                    .object(pathFrom + relativePath)
                                                    .build())
                                    .build());

                }
                catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                       InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
                    log.error("Error copying folder {} ", pathFrom);
                    throw new MinIoResourceException("Error copying folder " + pathTo + relativePath);
                }
            }

        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("Error copying folder {} from Minio", pathTo, e);
            throw new MinIoResourceException("Error copying folder " + pathTo);
        }


    }

    public void copyFile(String pathFrom, String pathTo) {
        log.debug("Copying file {} from Minio", pathFrom);
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(pathTo)
                            .source(
                                    CopySource.builder()
                                            .bucket(minioConfig.getBucketName())
                                            .object(pathFrom)
                                            .build())
                            .build());
        }
        catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
               InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | IOException e) {
            log.error("Error copying folder {} ", pathTo, e);
            throw new MinIoResourceException("Error copying file " + pathTo);
        }
    }

    public Iterable<Result<Item>> getAllUserResources(String userFolder) {

        log.debug("Getting all user resources from Minio");

        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(userFolder)
                        .recursive(true)
                        .build());
    }

    public Iterable<Result<Item>> getFolderResources(String path) {

        log.debug("Getting folder resources from Minio");

        return  minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(path)
                        .recursive(false) // Set false if you want to simulate folders
                        .build()
        );
    }
}

