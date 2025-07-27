package org.ek.cloud_storage.minio.services.bucket.helperClasses.impl;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.domain.resource.FileResource;
import org.ek.cloud_storage.minio.domain.resource.FolderResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.services.PathService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceInfoService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceValidationService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceInfoServiceImpl implements ResourceInfoService {

    private final MinioClient minioClient;
    private final MinioConfig  minioConfig;

    private final ResourceValidationService resourceValidationService;
    private final PathService pathService;

    @Override
    public Resource getResourceInfo(String path) throws IOException {
        resourceValidationService.validateResourceExists(path);

        if (resourceValidationService.isFolder(path)){
            return getFolderResource(path);

        }
        else {
            return getFileResource(path);
        }
    }

    private FolderResource getFolderResource(String path) throws IOException {

        resourceValidationService.folderExists(path);

        return new FolderResource(
                pathService.getFolderPath(path),
                pathService.getResourceName(path));
    }

    private FileResource getFileResource(String path) throws IOException {

        resourceValidationService.fileExists(path);

        Path pathObj = Paths.get(path);

        try {

            StatObjectResponse objectStat =
                    minioClient.statObject(StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(path)
                            .build());

            FileResource fileResource = new FileResource(
                    pathService.removeUserPrefixFromPath(path),
                    pathObj.getFileName().toString(),
                    objectStat.size()
            );

            return fileResource;

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Resource> listAllResources(String prefix) throws IOException {
        resourceValidationService.validateResourceExists(prefix);

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(prefix)
                        .recursive(false) // Set false if you want to simulate folders
                        .build()
        );

        List<Resource> resourceResponseList = new ArrayList<>();

        try {
            for (Result<Item> result : results) {
                Item item = result.get();

                if (item.isDir()) {
                    FolderResource folder = new FolderResource(
                            pathService.removeUserPrefixFromPath(item.objectName()),
                            pathService.getResourceName(item.objectName()));
                    resourceResponseList.add(folder);
                }
                else {
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
        }catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }

        return resourceResponseList;
    }

    @Override
    public List<Resource> searchResource(String userFolder, String query) throws IOException {
        List<Resource> results = new ArrayList<>();

        Iterable<Result<Item>> allFiles = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .prefix(userFolder)
                        .recursive(true)
                        .build());

        try {
            for (Result<Item> result : allFiles) {
                Item item = result.get();
                String objectName = item.objectName();

                //*Add folder

                String name;

                if (objectName.endsWith("/")) {
                    String substring  = objectName.substring(0, objectName.length() - 1);
                    name = substring.substring(substring.lastIndexOf("/")+1);

                    if (name.toLowerCase().contains(query.toLowerCase())) {


                        FolderResource folderResource = new FolderResource(
                                pathService.removeUserPrefixFromPath(objectName),
                                name + "/"
                        );
                        System.out.println("****************************************************");
                        System.out.println("****************************************************");
                        System.out.println("****************************************************");
                        System.out.println("Folder path - " + pathService.removeUserPrefixFromPath(objectName));
                        System.out.println("Folder name - " + name + "/");
                        System.out.println("****************************************************");
                        System.out.println("****************************************************");
                        System.out.println("****************************************************");
                        results.add(folderResource);

                    }

                    //**Add file
                }
                else{
                    name = objectName.substring(objectName.lastIndexOf("/")+1);

                    if (name.toLowerCase().contains(query.toLowerCase())) {


                        FileResource fileResource = new FileResource(
                                pathService.removeUserPrefixFromPath(objectName),
                                name,
                                item.size()
                        );
                        results.add(fileResource);

                    }

                }
            }

        }
        catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
               InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }

        return results;
    }
}
