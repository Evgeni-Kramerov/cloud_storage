package org.ek.cloud_storage.minio.services;

import org.ek.cloud_storage.minio.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.resource.DownloadResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public interface BucketService {

    //**Buckets

    void createBucket(String bucketName) throws IOException;

    boolean bucketExists(String bucketName) throws IOException;

    //**Resources

    Resource getResourceInfo(String path) throws IOException;

    void deleteResource(String path) throws IOException;

    List<Resource> searchResource(String userFolder, String query) throws IOException;

    List<Resource> listAllResources(String prefix) throws IOException;

    DownloadResource downloadResource(String path) throws IOException;

    DownloadResource downloadFile(String path);

    void uploadResource(String path, MultipartFile file) throws IOException;

    void moveResource(String oldPath, String newPath) throws IOException;

    boolean fileExists(String path) throws IOException;

    void validateResourceExists(String path) throws IOException;

    //**Folders

    void createEmptyFolder(String path) throws IOException;

    void getFolderInfo(String path) throws IOException;

    boolean folderExists(String path) throws IOException;
}
