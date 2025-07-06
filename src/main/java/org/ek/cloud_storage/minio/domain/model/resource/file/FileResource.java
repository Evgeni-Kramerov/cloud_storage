package org.ek.cloud_storage.minio.domain.model.resource.file;

import org.ek.cloud_storage.minio.domain.model.resource.Resource;
import org.ek.cloud_storage.minio.domain.model.resource.ResourceTypeENUM;

public class FileResource extends Resource {
    private long size;

    public FileResource(String path, String name, long size) {
        super(path,name, ResourceTypeENUM.FILE);
        this.size = size;
    }
}
