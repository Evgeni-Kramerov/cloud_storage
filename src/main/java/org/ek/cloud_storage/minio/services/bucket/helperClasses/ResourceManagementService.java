package org.ek.cloud_storage.minio.services.bucket.helperClasses;

import org.ek.cloud_storage.minio.domain.resource.Resource;

import java.io.IOException;

public interface ResourceManagementService {
    void moveResource(String oldPath, String newPath) throws IOException;

    void deleteResource(String path) throws IOException;

    Resource createEmptyFolder(String path) throws IOException;

}
