package org.ek.cloud_storage.minio.domain.dto;


import lombok.Getter;
import lombok.Setter;
import org.ek.cloud_storage.minio.domain.ResourceTypeENUM;

@Getter
@Setter
public class FileResourceResponseDTO extends ResourceResponseDTO {
    private long size;

    public FileResourceResponseDTO(String path, String name, long size) {
        super(path, name, ResourceTypeENUM.FILE);
        this.size = size;
    }
}
