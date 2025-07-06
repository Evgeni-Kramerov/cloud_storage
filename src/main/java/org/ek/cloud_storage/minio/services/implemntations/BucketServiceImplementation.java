package org.ek.cloud_storage.minio.services.implemntations;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.ek.cloud_storage.minio.domain.dto.ResourceInfoResponseDTO;
import org.ek.cloud_storage.minio.domain.model.resource.ResourceTypeENUM;
import org.ek.cloud_storage.minio.services.BucketService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class BucketServiceImplementation implements BucketService {

    private final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://localhost:9000/")
                    .credentials("minioadmin",
                            "minioadmin")
                    .build();

    private final String bucketName = "user-files";


    @Override
    public boolean bucketExists(String bucketName) throws IOException {

        try {
        return minioClient.bucketExists(
                BucketExistsArgs
                        .builder()
                        .bucket(bucketName)
                        .build());

        }    catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                    InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void createBucket(String bucketName) throws IOException {
        try {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }   catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                   InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void moveResource(String oldPath, String newPath) throws IOException {

        try {
            // Create object "my-objectname" in bucket "my-bucketname" by copying from object
            // "my-objectname" in bucket "my-source-bucketname".
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(oldPath)
                                            .build())
                            .build());

            deleteResource(oldPath);

        }
        catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
               InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void uploadResource(String path, MultipartFile file) throws IOException {


        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

        }
        catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
               InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadResource(String pathCloudObject, String pathLocalObject) throws IOException {

        try {
        minioClient.downloadObject(
                DownloadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(pathCloudObject)
//                        .filename(pathLocalObject)
                        .build());
        }  catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                  InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public ResourceInfoResponseDTO getResourceInfo(String path) throws IOException {

        Path pathObj = Paths.get(path);

        try {

        StatObjectResponse objectStat =
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());

        ResourceInfoResponseDTO resourceInfoResponseDTO = new ResourceInfoResponseDTO();

        resourceInfoResponseDTO.setName(pathObj.getFileName().toString());
        resourceInfoResponseDTO.setPath(pathObj.getParent().toString().replace("^/", "") +"/");
        resourceInfoResponseDTO.setSize(objectStat.size());
        resourceInfoResponseDTO.setResourceType(ResourceTypeENUM.FILE);

        return resourceInfoResponseDTO;

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteResource(String path) throws IOException {

        try {

        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ResourceInfoResponseDTO> searchResource(String name) throws IOException {

        List<ResourceInfoResponseDTO> results = new ArrayList<>();

        Iterable<Result<Item>> allFiles = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build());

        StreamSupport.stream(allFiles.spliterator(), false)
                .map(result -> {
                    try {
                        return result.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(item -> {
                    String objectname = item.objectName();
                    String fileName = objectname.substring(objectname.lastIndexOf("/")+1).toLowerCase();
                    return fileName.contains(name.toLowerCase());
                })
                .forEach(item -> {
                    ResourceInfoResponseDTO result = new ResourceInfoResponseDTO();
                    result.setPath(item.objectName());
                    result.setName(item.objectName());
                    result.setSize(item.size());
                    result.setResourceType(ResourceTypeENUM.FILE);
                    results.add(result);
                });

        return results;
    }

    @Override
    public void createEmptyFolder(String path) throws IOException {

        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .stream(emptyStream, 0, -1)
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
        throw new RuntimeException(e);
    }
    }

    @Override
    public void getFolderInfo(String path) throws IOException {

        try {


            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                System.out.println(item.objectName());
            }
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }

    }

    public InputStream downloadObject(String path) {

        try{
        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build()
        );

       return inputStream;
    } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
        System.out.println("Error occurred: " + e);
    } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
