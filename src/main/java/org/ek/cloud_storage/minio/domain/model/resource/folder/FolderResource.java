package org.ek.cloud_storage.minio.domain.model.resource.folder;

import org.ek.cloud_storage.minio.domain.model.resource.Resource;
import org.ek.cloud_storage.minio.domain.model.resource.ResourceTypeENUM;

public class FolderResource extends Resource {
    public FolderResource(String path, String name, long size) {
        super(path,name, ResourceTypeENUM.FOLDER);
    }
}
