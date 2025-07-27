package org.ek.cloud_storage.minio.services.bucket.helperClasses;

import org.ek.cloud_storage.minio.domain.resource.Resource;

import java.io.IOException;

public interface ResourceValidationService {

        void validateResourceExists(String path) throws IOException;

        boolean booleanValidateResourceExists(String path) throws IOException;

        boolean resourceExists(String path)  throws IOException;

        boolean fileExists(String path) throws IOException;

        boolean folderExists(String path) throws IOException;

        boolean isFolder(String path)  throws IOException;

}
