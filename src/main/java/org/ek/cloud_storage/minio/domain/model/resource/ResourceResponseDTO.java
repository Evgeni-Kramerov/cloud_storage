package org.ek.cloud_storage.minio.domain.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponseDTO {
    private String path;
    private String name;
    private ResourceTypeENUM type;
}
