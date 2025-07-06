package org.ek.cloud_storage.minio.domain.model.resource.file;


import org.ek.cloud_storage.minio.domain.model.resource.ResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.model.resource.ResourceTypeENUM;

public class FileResourceResponseDTO extends ResourceResponseDTO {
    private long size;

    public FileResourceResponseDTO(String path, String name, long size) {
        super(path, name, ResourceTypeENUM.FILE);
        this.size = size;
    }
}
