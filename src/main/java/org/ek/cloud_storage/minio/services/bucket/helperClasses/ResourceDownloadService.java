package org.ek.cloud_storage.minio.services.bucket.helperClasses;

import org.ek.cloud_storage.minio.domain.resource.DownloadResource;

import java.io.IOException;

public interface ResourceDownloadService {
    DownloadResource downloadResource(String path) throws IOException;
}
