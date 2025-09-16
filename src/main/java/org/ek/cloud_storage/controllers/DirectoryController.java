package org.ek.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.domain.resource.Resource;
import org.ek.cloud_storage.mappers.ResourceMapper;
import org.ek.cloud_storage.services.PathService;
import org.ek.cloud_storage.services.BucketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final BucketService bucketService;

    private final ResourceMapper resourceMapper;


    @GetMapping
    public ResponseEntity<List<ResourceResponseDTO>> getDirectoryInfo(
            Principal principal,
            String path) throws IOException {

        log.info("Received GET request getDirectoryInfo - {}", path);

        List<Resource> resources = bucketService.listAllResources(principal, path);

        List<ResourceResponseDTO> response = resourceMapper.resourceListToDtoList(resources);

        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createDirectory(
            Principal principal,
            String path
    ) {

        log.info("Received POST request createDirectory - {}", path);

        Resource createdFolder = bucketService.createEmptyFolder(principal, path);

        ResourceResponseDTO response = resourceMapper.resourceToResourceResponseDTO(createdFolder);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
