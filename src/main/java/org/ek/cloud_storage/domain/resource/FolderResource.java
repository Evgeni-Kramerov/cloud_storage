package org.ek.cloud_storage.domain.resource;

import lombok.Getter;
import lombok.Setter;
import org.ek.cloud_storage.domain.ResourceTypeENUM;

@Getter
@Setter
public class FolderResource extends Resource {
    public FolderResource(String path, String name) {
        super(path,name, ResourceTypeENUM.DIRECTORY);
    }
}
