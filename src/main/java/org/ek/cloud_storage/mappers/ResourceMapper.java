package org.ek.cloud_storage.mappers;

import org.ek.cloud_storage.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.domain.resource.FileResource;
import org.ek.cloud_storage.domain.dto.FileResourceResponseDTO;
import org.ek.cloud_storage.domain.resource.FolderResource;
import org.ek.cloud_storage.domain.dto.FolderResourceResponseDTO;
import org.ek.cloud_storage.domain.resource.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResourceMapper {

    List<ResourceResponseDTO> resourceListToDtoList(List<Resource> resourceList);

    FolderResourceResponseDTO folderToFolderResourceResponseDTO(FolderResource folderResource);

    FileResourceResponseDTO fileToFileResourceResponseDTO(FileResource fileResource);

    //logic for list mapping
    default ResourceResponseDTO resourceToResourceResponseDTO(Resource resource) {
        if (resource instanceof FolderResource) {
            return folderToFolderResourceResponseDTO((FolderResource) resource);
        }
        else if (resource instanceof FileResource) {
            return fileToFileResourceResponseDTO((FileResource) resource);
        }
        throw new IllegalArgumentException("Invalid resource type");
    }
}
