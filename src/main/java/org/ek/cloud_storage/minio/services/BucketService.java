package org.ek.cloud_storage.minio.services;

import org.ek.cloud_storage.minio.domain.dto.ResourceInfoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public interface BucketService {


    void createBucket(String bucketName) throws IOException;

    boolean bucketExists(String bucketName) throws IOException;

    //**Resources

    ResourceInfoResponseDTO getResourceInfo(String path) throws IOException;

    void deleteResource(String path) throws IOException;

    List<ResourceInfoResponseDTO> searchResource(String name) throws IOException;

    void downloadResource(String pathCloudObject, String pathLocalObject) throws IOException;

    InputStream downloadObject(String path);

    void uploadResource(String path, MultipartFile file) throws IOException;

    void moveResource(String oldPath, String newPath) throws IOException;

    //**Folders

    void createEmptyFolder(String path) throws IOException;

    void getFolderInfo(String path) throws IOException;

}
