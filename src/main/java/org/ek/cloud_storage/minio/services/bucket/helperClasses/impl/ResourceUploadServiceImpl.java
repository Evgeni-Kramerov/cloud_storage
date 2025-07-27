package org.ek.cloud_storage.minio.services.bucket.helperClasses.impl;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.domain.resource.FileResource;
import org.ek.cloud_storage.minio.domain.resource.FolderResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceUploadService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceValidationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceUploadServiceImpl implements ResourceUploadService {

    private final MinioClient minioClient;

    private final MinioConfig  minioConfig;

    private final ResourceValidationService resourceValidationService;

    @Override
    public List<Resource> uploadResource(String path, List<MultipartFile> files) {
        Set<String> folders = new HashSet<>();
        List<Resource> uploaded = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalPath = file.getOriginalFilename();
            if (originalPath == null || file.isEmpty()) continue;

            String objectName = path + "/" + originalPath.replace("\\", "/");

            // Track all folder parts
            String[] parts = originalPath.split("[/\\\\]");
            StringBuilder folderPath = new StringBuilder(path);
            for (int i = 0; i < parts.length - 1; i++) {
                folderPath.append("/").append(parts[i]);
                folders.add(folderPath.toString());
            }

            // Upload the file
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );

                uploaded.add(new FileResource(objectName, file.getOriginalFilename(), file.getSize()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload " + originalPath, e);
            }
        }

        // Optionally create folder placeholders
        for (String folder : folders) {
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
                // ignore or log
            }
            uploaded.add(new FolderResource(folder + "/", folder));
        }

        return uploaded;
    }
}

