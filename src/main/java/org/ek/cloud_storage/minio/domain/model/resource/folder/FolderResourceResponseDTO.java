package org.ek.cloud_storage.minio.domain.model.resource.folder;


import org.ek.cloud_storage.minio.domain.model.resource.ResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.model.resource.ResourceTypeENUM;

public class FolderResourceResponseDTO extends ResourceResponseDTO {
    public FolderResourceResponseDTO(String path, String name, long size) {
        super(path, name, ResourceTypeENUM.FOLDER);
    }
}
