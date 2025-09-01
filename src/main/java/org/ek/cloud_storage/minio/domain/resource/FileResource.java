package org.ek.cloud_storage.minio.domain.resource;

import lombok.Getter;
import lombok.Setter;
import org.ek.cloud_storage.minio.domain.ResourceTypeENUM;

@Getter
@Setter
public class FileResource extends Resource {
    private long size;

    public FileResource(String path, String name, long size) {
        super(path,name, ResourceTypeENUM.FILE);
        this.size = size;
    }
}
