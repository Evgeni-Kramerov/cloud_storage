package org.ek.cloud_storage.domain.dto;


import lombok.Getter;
import lombok.Setter;
import org.ek.cloud_storage.domain.ResourceTypeENUM;

@Getter
@Setter
public class FolderResourceResponseDTO extends ResourceResponseDTO {
    public FolderResourceResponseDTO(String path, String name) {
        super(path, name, ResourceTypeENUM.DIRECTORY);
    }
}
