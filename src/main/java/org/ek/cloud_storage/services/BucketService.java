package org.ek.cloud_storage.services;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.config.MinioConfig;
import org.ek.cloud_storage.domain.resource.FileResource;
import org.ek.cloud_storage.domain.resource.FolderResource;
import org.ek.cloud_storage.exception.MinIoResourceException;
import org.ek.cloud_storage.domain.resource.DownloadResource;
import org.ek.cloud_storage.domain.resource.Resource;
import org.ek.cloud_storage.repositories.MinioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BucketService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private final MinioRepository minioRepository;

    private final PathService pathService;


    //**BucketManagementService

    public void createBucketIfNotExists()  {
        log.info("Creating bucket if not exists");
        try {
            boolean result;
            try {
                result = minioClient.bucketExists(
                        BucketExistsArgs
                                .builder()
                                .bucket(minioConfig.getBucketName())
                                .build());

            } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                     InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                log.error("Create bucket if not exists error", e);
                throw new MinIoResourceException("Failed to check if bucket exists " + e);
            }
            if (result)
                return;

            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | IOException e) {
            log.error("Create bucket if not exists error", e);
            throw new MinIoResourceException("Failed to create bucket " + e);
        }
    }

    //**ResourceInfoService

    public Resource getResourceInfo(Principal principal, String path) {

        String fullPath = pathService.getFullPathForUser(principal, path);

        if (isFolder(fullPath)) {
            return minioRepository.getFolder(fullPath);
        }
        return minioRepository.getFile(fullPath);

    }

    private boolean isFolder(String path) {
        return path.endsWith("/");
    }

    public List<Resource> listAllResources(Principal principal, String path) {

        String fullPath = pathService.getFullPathForUser(principal,path);

        List<Resource> resourceResponseList = new ArrayList<>();

        Iterable<Result<Item>> results = minioRepository.getFolderResources(fullPath);

        try {
            for (Result<Item> result : results) {
                Item item = result.get();

                if (item.isDir()) {
                    FolderResource folder = new FolderResource(
                            pathService.removeUserPrefixFromPath(item.objectName()),
                            pathService.getResourceName(item.objectName()));
                    resourceResponseList.add(folder);
                } else {
                    //check for empty files that are created as empty folders
                    if (item.size() == 0)
                        continue;
                    FileResource file = new FileResource(
                            pathService.removeUserPrefixFromPath(item.objectName()),
                            pathService.getResourceName(item.objectName()),
                            item.size()
                    );
                    resourceResponseList.add(file);

                }

            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | IOException e) {
            log.error("Error listing all resources ", e);
            throw new MinIoResourceException("Error listing all resources");
        }

        return resourceResponseList;
    }


    public List<Resource> searchResource(Principal principal, String query) {

        String userFolder = pathService.getUserFilesPrefix(principal);
        Iterable<Result<Item>> allFiles = minioRepository.getAllUserResources(userFolder);

        List<Resource> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        try {
            for (Result<Item> result : allFiles) {
                Item item = result.get();
                String objectName = item.objectName();
                String name = extractName(objectName);

                if (name.toLowerCase().contains(lowerQuery)) {
                    Resource resource = item.isDir()
                            ? buildFolderResource(objectName, name)
                            : buildFileResource(objectName, name, item.size());

                    results.add(resource);
                }

            }
        } catch (Exception e) {
            log.error("Error searching resources ", e);
            throw new MinIoResourceException("Error checking folder existence: " + userFolder);
        }
        return results;
    }

    private Resource buildFolderResource (String objectName, String name){
            return new FolderResource(
                    pathService.removeUserPrefixFromPath(objectName),
                    name + "/");
        }

    private Resource buildFileResource (String objectName, String name, long size){
            return new FileResource(
                    pathService.removeUserPrefixFromPath(objectName),
                    name,
                    size);
        }

    private String extractName (String objectName){

            String normalized = objectName.endsWith("/")
                    ? objectName.substring(0, objectName.length() - 1)
                    : objectName;

            return normalized.substring(normalized.lastIndexOf("/") + 1);
        }

    //**ResourceUploadService

    public List<Resource> uploadResource (Principal principal,  String path, MultipartFile[] files) {

        log.info("Uploading files to bucket");

        List<MultipartFile> multipartFiles = Arrays.asList(files);
        String fullPath =  pathService.getFullPathForUser(principal, path);

        Set<String> folders = new HashSet<>();
        List<Resource> uploaded = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            String originalPath = file.getOriginalFilename();

            if (originalPath == null || file.isEmpty())  continue;

            String objectName = fullPath + "/" + originalPath.replace("\\", "/");

            // Track all folder parts
            String[] parts = originalPath.split("[/\\\\]");

            StringBuilder folderPath = new StringBuilder(fullPath);
            for (int i = 0; i < parts.length - 1; i++) {
                folderPath.append("/").append(parts[i]);
                folders.add(folderPath.toString());
            }

            // Upload the file
            minioRepository.saveFile(file, objectName);

            uploaded.add(new FileResource(objectName, file.getOriginalFilename(), file.getSize()));
            }

            for (String folder : folders) {
                minioRepository.creatEmptyFolder(folder);
                uploaded.add(new FolderResource(folder + "/", folder));
            }
            return uploaded;
        }


    //**ResourceDownloadService

    public DownloadResource downloadResource (Principal principal, String path){
        log.info("Downloading files from  bucket {}", path);
            String fullPath = pathService.getFullPathForUser(principal, path);

            if (isFolder(fullPath)) {
                return minioRepository.downloadFolder(fullPath);
            }

            return minioRepository.downloadFile(fullPath);
    }

        //**ResourceManagementService

    public Resource moveResource (Principal principal, String from, String to) {

        log.info("Moving files from {} to {}", from, to);

            String fullPathFrom = pathService.getFullPathForUser(principal, from);
            String fullPathTo = pathService.getFullPathForUser(principal, to);

            if (isFolder(fullPathFrom)) {
                minioRepository.copyFolder(fullPathFrom, fullPathTo);
                minioRepository.deleteFolder(fullPathTo);
                return minioRepository.getFolder(fullPathTo);
            } else {
                minioRepository.copyFile(fullPathFrom, fullPathTo);
                minioRepository.deleteFile(fullPathTo);
                return minioRepository.getFile(fullPathTo);
            }

        }

    public void deleteResource (Principal principal, String path) {

        log.info("Deleting files from {}", path);
            String fullPath = pathService.getFullPathForUser(principal, path);

            if (isFolder(fullPath)) {
                minioRepository.deleteFolder(fullPath);
            } else {
                minioRepository.deleteFile(fullPath);
            }
        }

    public Resource createEmptyFolder (Principal principal, String path) {

        log.info("Creating empty folder {}", path);

        String fullPath = pathService.getFullPathForUser(principal,path);

        minioRepository.creatEmptyFolder(fullPath);

        return new FolderResource(
                pathService.getFolderPath(fullPath),
                pathService.getResourceName(fullPath));
        }
    }