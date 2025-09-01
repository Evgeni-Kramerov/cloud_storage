package org.ek.cloud_storage.minio.services.bucket.helperClasses;

import org.ek.cloud_storage.minio.domain.resource.Resource;

import java.io.IOException;
import java.util.List;

public interface ResourceInfoService {

    Resource getResourceInfo(String path) throws IOException;

    List<Resource> listAllResources(String prefix) throws IOException;

    List<Resource> searchResource(String userFolder, String query) throws IOException;

}
