package org.ek.cloud_storage.minio.services.bucket.helperClasses;

import java.io.IOException;

public interface BucketManagementService {
    void createBucketIfNotExists() throws IOException;
}
