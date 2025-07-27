package org.ek.cloud_storage.minio.services.bucket;

import org.ek.cloud_storage.minio.domain.resource.DownloadResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BucketService {

    //**Buckets
    void createBucketIfNotExists() throws IOException;

    //**Resources

    Resource getResourceInfo(String path) throws IOException;
    void deleteResource(String path) throws IOException;


    //The same - make list all as search ???
    List<Resource> searchResource(String userFolder, String query) throws IOException;
    List<Resource> listAllResources(String prefix) throws IOException;


    DownloadResource downloadResource(String path) throws IOException;
    List<Resource> uploadResource(String path, List<MultipartFile> files) throws IOException;

    void moveResource(String oldPath, String newPath) throws IOException;

    Resource createEmptyFolder(String path) throws IOException;

}
