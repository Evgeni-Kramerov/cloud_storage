package org.ek.cloud_storage.minio.services.bucket.helperClasses;

import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceUploadService {
    List<Resource> uploadResource(String path, List<MultipartFile> files);
}
