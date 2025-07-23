package org.ek.cloud_storage.minio.controllers;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.resource.FolderResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.mappers.ResourceMapper;
import org.ek.cloud_storage.minio.services.BucketService;
import org.ek.cloud_storage.minio.services.PathService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final BucketService bucketService;

    private final ResourceMapper resourceMapper;

    private final PathService pathService;


    @GetMapping
    public ResponseEntity<List<ResourceResponseDTO>> getDirectoryInfo(
            Principal principal,
            String path) throws IOException {

        String fullPath = pathService.fullPathForUser(principal,path);

        bucketService.validateResourceExists(fullPath);

        List<Resource> resources = bucketService.listAllResources(fullPath);

        List<ResourceResponseDTO> response = resourceMapper.resourceListToDtoList(resources);

        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createDirectory(
            Principal principal,
            String path
    )  throws IOException {

        String fullPath = pathService.fullPathForUser(principal,path);

        System.out.println("In create empty folder controller");

        System.out.println(path);

        //TODO Return Whats needed for each method

        bucketService.createEmptyFolder(fullPath);


        Resource createdFolder = bucketService.getResourceInfo(fullPath);

        ResourceResponseDTO response = resourceMapper.resourceToResourceResponseDTO(createdFolder);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
