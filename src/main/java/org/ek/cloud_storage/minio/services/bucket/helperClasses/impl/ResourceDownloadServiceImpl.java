package org.ek.cloud_storage.minio.services.bucket.helperClasses.impl;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.domain.resource.DownloadResource;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.ResourceDownloadService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ResourceDownloadServiceImpl implements ResourceDownloadService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private final ResourceValidationServiceImpl resourceValidationService;

    @Override
    public DownloadResource downloadResource(String path) throws IOException {
        resourceValidationService.validateResourceExists(path);

        if (resourceValidationService.isFolder(path)) {
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
    }

    private DownloadResource downloadFile(String path) {

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
                throw new IOException(e);
            }
        };

        String fileName = Paths.get(path).getFileName().toString();

        return new DownloadResource(fileName, stream);

    }
}
