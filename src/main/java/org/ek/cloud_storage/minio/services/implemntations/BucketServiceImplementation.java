package org.ek.cloud_storage.minio.services.implemntations;

import com.google.common.base.Strings;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.resource.DownloadResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.domain.resource.FileResource;
import org.ek.cloud_storage.minio.domain.dto.FileResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.resource.FolderResource;
import org.ek.cloud_storage.minio.exception.MinIoResourceErrorException;
import org.ek.cloud_storage.minio.mappers.ResourceMapper;
import org.ek.cloud_storage.minio.services.BucketService;
import org.ek.cloud_storage.minio.services.PathService;
import org.springframework.stereotype.Service;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class BucketServiceImplementation implements BucketService {

    private final PathService pathService;

    private final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://localhost:9000/")
                    .credentials("minioadmin",
                            "minioadmin")
                    .build();

    private final String bucketName = "user-files";


    @Override
    public void validateResourceExists(String path) throws IOException {
        try {

            if (path.endsWith("/")) {
                if (!folderExists(path))
                    throw new MinIoResourceErrorException("Folder does not exist");
            }

            else {
                if (!fileExists(path))
                    throw new MinIoResourceErrorException("File does not exist");
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean fileExists(String path) throws IOException {

        System.out.println("file exists");

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return true;

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new RuntimeException("MinIO error", e);

        } catch (Exception e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }

    @Override
    public boolean folderExists(String path) throws IOException {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path.endsWith("/") ? path : path + "/")
                        .maxKeys(1)
                        .build()
        );

        try {
            return results.iterator().hasNext(); // If at least one object exists under the prefix
        } catch (Exception e) {
            throw new RuntimeException("Failed to check folder existence", e);
        }
    }

    @Override
    public List<Resource> listAllResources(String prefix) throws IOException {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
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
                            pathService.removeUserFolderFromPath(item.objectName()),
                            pathService.getResourceName(item.objectName()));
                    resourceResponseList.add(folder);
                }
                else {
                    //check for empty files that are created as empty folders
                    if (item.size() == 0)
                        continue;
                    FileResource file = new FileResource(
                            pathService.removeUserFolderFromPath(item.objectName()),
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

        validateResourceExists(oldPath);

        if (isFolder(oldPath)) {
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

    private void copyFolder(String oldPath, String newPath) {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(oldPath)
                        .recursive(true)
                        .build()
        );

        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                String relativePath = item.objectName().substring(oldPath.length());
                copyFile(oldPath + relativePath, newPath + relativePath);
            }

        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    @Override
    public List<Resource> uploadResource(String path, List<MultipartFile> files) throws IOException {

        System.out.println("In upload Resource function");

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
                                .bucket(bucketName)
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
                                .bucket(bucketName)
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

    @Override
    public DownloadResource downloadResource(String path) throws IOException {

        validateResourceExists(path);

        if (isFolder(path)) {
            return downloadFolder(path);

        }

        else {
            return downloadFile(path);
        }

    }

    private DownloadResource downloadFolder(String path) {

        String finalPath = path.endsWith("/")  ? path : path + "/" ;

        StreamingResponseBody zipStream = outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                zipFolderFromMinIO(finalPath,zipOut);
            } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                     InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
                throw new RuntimeException(e);
            }
        };

        String zipFileName = Paths.get(path).getFileName().toString() + ".zip";

        return new DownloadResource(zipFileName, zipStream);
    }

    private void zipFolderFromMinIO(String path, ZipOutputStream zipOut) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
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
                            .bucket(bucketName)
                            .object(item.objectName())
                            .build()
            )) {
                zipOut.putNextEntry(new ZipEntry(relativePath));
                stream.transferTo(zipOut);
                zipOut.closeEntry();
            }
        }
    }

    public DownloadResource downloadFile(String path) {

        StreamingResponseBody stream = outputStream -> {
            try(InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            )) {
                inputStream.transferTo(outputStream);
            } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                     InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e){
                throw new IOException(e);
            }
        };

        String fileName = Paths.get(path).getFileName().toString();

        return new DownloadResource(fileName, stream);

    }



    @Override
    public Resource getResourceInfo(String path) throws IOException {
        System.out.println("getResourceInfo");

        validateResourceExists(path);

        if (isFolder(path)){
            System.out.println("getResourceInfo - folder");
            return getFolderResource(path);

        }
        else {
            System.out.println("getResourceInfo - file");
            return getFileResource(path);
        }
    }

    private FolderResource getFolderResource(String path) throws IOException {

        System.out.println("getFolderResource");

        folderExists(path);

        FolderResource folder = new FolderResource(
                pathService.getFolderPath(path),
                pathService.getResourceName(path));

        return folder;
    }

    private FileResource getFileResource(String path) throws IOException {

        System.out.println("getFileResource");

        fileExists(path);

        Path pathObj = Paths.get(path);

        try {

            StatObjectResponse objectStat =
                    minioClient.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            FileResource fileResource = new FileResource(
                    pathService.removeUserFolderFromPath(path),
                    pathObj.getFileName().toString(),
                    objectStat.size()
            );

            return fileResource;

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isFolder(String path) {
        String prefix = path.endsWith("/") ? path : path + "/";

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .maxKeys(1)
                        .build()
        );

        return results.iterator().hasNext();
    }

    @Override
    public void deleteResource(String path) throws IOException {

        validateResourceExists(path);

        System.out.println(path);

        if (isFolder(path)){
            System.out.println("deleteResource - folder");
            deleteFolder(path);
        }

        else {
            System.out.println("deleteResource - file");
            deleteFile(path);
        }


    }

    private void deleteFolder(String path) throws IOException {

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
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
                                .bucket(bucketName)
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
                    .bucket(bucketName)
                    .object(path)
                    .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Resource> searchResource(String userFolder, String query) throws IOException {

        List<Resource> results = new ArrayList<>();

        Set<String> matchedFolders = new HashSet<>();

        Iterable<Result<Item>> allFiles = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(userFolder)
                        .recursive(true)
                        .build());
        try {
            for (Result<Item> result : allFiles) {
                Item item = result.get();
                String objectName = item.objectName();

                String name = objectName.substring(objectName.lastIndexOf("/"));
                System.out.println("-------------New Object");
                System.out.println("Name " + name);


                if (name.toLowerCase().contains(query.toLowerCase())) {
                        FileResource fileResource = new FileResource(
                                pathService.removeUserFolderFromPath(objectName),
                                name,
                                item.size()
                        );
                        results.add(fileResource);


                    }

                Path parentPath = Paths.get(objectName).getParent();

                while (parentPath != null) {
                    matchedFolders.add(parentPath.toString().replace("\\", "/") + "/");
                    parentPath = parentPath.getParent();
                    }
                }

            for (String folderPath : matchedFolders) {

                String folderName = pathService.getResourceName(folderPath);

                //TODO Parent folder Path correct

                Path folderPathCorrect = Paths.get(pathService.removeUserFolderFromPath(folderPath))
                        .normalize();
                Path parentPath = folderPathCorrect.getParent();

                if (parentPath == null) {
                    parentPath = Path.of("/");
                }

                System.out.println("Folder full path " + folderPathCorrect);
                System.out.println("Folder parent Path" + parentPath);


                if (folderName.toLowerCase().contains(query.toLowerCase())) {
                    FolderResource folder = new FolderResource(
                            parentPath.toString(),
                            folderName
                    );
                    results.add(folder);
                }
            }
        }
        catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
               InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }



        return results;
    }

    @Override
    public void createEmptyFolder(String path) throws IOException {

        if (folderExists(path)) {
            throw new MinIoResourceErrorException("Folder already exists");
        }

        if (!folderExists(pathService.getParentFolderPath(path))) {
            throw new MinIoResourceErrorException("Parent folder doesnt exists");
        }


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



}
