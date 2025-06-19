package org.ek.cloud_storage.services.implemntations;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.ek.cloud_storage.domain.dto.ResourceInfoResponseDTO;
import org.ek.cloud_storage.domain.model.ResourceTypeENUM;
import org.ek.cloud_storage.services.BucketService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.StreamSupport;

public class BucketServiceImplementation implements BucketService {

    private final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://localhost:9000/")
                    .credentials("minioadmin",
                            "minioadmin")
                    .build();

    private final String bucketName = "user1";

    @Override
    public void downloadResource(String path) throws IOException {

        //Change object name and saving with the same name, now hardcoded


        try {
        minioClient.downloadObject(
                DownloadObjectArgs.builder()
                        .bucket(bucketName)
                        .object("folder123/Test-123.txt")
                        .filename("my-object-file")
                        .build());
        }  catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                  InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void getResourceInfo(String path) throws IOException {

        Path pathObj = Paths.get(path);

//        listObjects(ListObjectsArgs args)
//        statObject(StatObjectArgs args)
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

        System.out.println(resourceInfoResponseDTO);

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteResource(String path) throws IOException {
        Path pathObj = Paths.get(path);
        try{

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
    public void searchResource(String name) throws IOException {
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
                    System.out.println(item.objectName());
                });
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

    public void downloadObject(String path) {

        try{
        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build()
        );

        byte[] buf = new byte[16384];
        int bytesRead;
        while ((bytesRead = inputStream.read(buf, 0, buf.length)) >= 0) {
            System.out.println(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
        }

        // Close the input stream.
        inputStream.close();
            System.out.println("Input stream closed");
    } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
        System.out.println("Error occurred: " + e);
    } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        BucketServiceImplementation bucketServiceImplementation = new BucketServiceImplementation();


//        bucketServiceImplementation.searchResource("Test-123.txt");
        bucketServiceImplementation.downloadResource("folder123/Test-123.txt");

        System.exit(0);

    }
}
