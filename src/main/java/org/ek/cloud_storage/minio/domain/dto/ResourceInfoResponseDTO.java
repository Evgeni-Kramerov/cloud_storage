package org.ek.cloud_storage.minio.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ek.cloud_storage.minio.domain.model.resource.ResourceTypeENUM;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInfoResponseDTO {
    private String path;
    private String name;
    private long size;
    private ResourceTypeENUM resourceType;
}
