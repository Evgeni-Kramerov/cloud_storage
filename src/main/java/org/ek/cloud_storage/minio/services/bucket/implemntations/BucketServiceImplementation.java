package org.ek.cloud_storage.minio.services.bucket.implemntations;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.config.MinioConfig;
import org.ek.cloud_storage.minio.domain.resource.DownloadResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.domain.resource.FileResource;
import org.ek.cloud_storage.minio.domain.resource.FolderResource;
import org.ek.cloud_storage.minio.exception.MinIoResourceErrorException;
import org.ek.cloud_storage.minio.services.bucket.BucketService;
import org.ek.cloud_storage.minio.services.PathService;
import org.ek.cloud_storage.minio.services.bucket.helperClasses.*;
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
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class BucketServiceImplementation implements BucketService {

    private final BucketManagementService bucketManagementService;
    private final ResourceInfoService resourceInfoService;
    private final ResourceUploadService  resourceUploadService;
    private final ResourceDownloadService resourceDownloadService;
    private final ResourceManagementService resourceManagementService;

    //**BucketManagementService

    @Override
    public void createBucketIfNotExists() throws IOException {
        bucketManagementService.createBucketIfNotExists();
    }

    //**ResourceInfoService

    @Override
    public Resource getResourceInfo(String path) throws IOException {
        return resourceInfoService.getResourceInfo(path);
    }

    @Override
    public List<Resource> listAllResources(String prefix) throws IOException {
        return resourceInfoService.listAllResources(prefix);
    }


    //TODO - incorrect folder in search - can`t access
    @Override
    public List<Resource> searchResource(String userFolder, String query) throws IOException {
        return  resourceInfoService.searchResource(userFolder, query);
    }


    //**ResourceUploadService

    @Override
    public List<Resource> uploadResource(String path, List<MultipartFile> files) throws IOException {
        return resourceUploadService.uploadResource(path, files);
    }

    //**ResourceDownloadService

    @Override
    public DownloadResource downloadResource(String path) throws IOException {
        return resourceDownloadService.downloadResource(path);
    }

    //**ResourceManagementService

    @Override
    public void moveResource(String oldPath, String newPath) throws IOException {
        resourceManagementService.moveResource(oldPath, newPath);
    }

    @Override
    public void deleteResource(String path) throws IOException {
        resourceManagementService.deleteResource(path);
    }

    @Override
    public Resource createEmptyFolder(String path) throws IOException {
        return resourceManagementService.createEmptyFolder(path);
    }

}
