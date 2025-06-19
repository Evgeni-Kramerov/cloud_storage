package org.ek.cloud_storage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ek.cloud_storage.domain.model.ResourceTypeENUM;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInfoResponseDTO {
    private String path;
    private String name;
    private long size;
    private ResourceTypeENUM resourceType;
}
