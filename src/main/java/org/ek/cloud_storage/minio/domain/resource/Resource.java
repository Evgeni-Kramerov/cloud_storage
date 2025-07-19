package org.ek.cloud_storage.minio.domain.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ek.cloud_storage.minio.domain.ResourceTypeENUM;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private String path;
    private String name;
    private ResourceTypeENUM type;
}
