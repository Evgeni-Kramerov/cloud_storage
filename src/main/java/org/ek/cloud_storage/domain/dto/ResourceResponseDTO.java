package org.ek.cloud_storage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ek.cloud_storage.domain.ResourceTypeENUM;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponseDTO {
    private String path;
    private String name;
    private ResourceTypeENUM type;
}
