package org.ek.cloud_storage.minio.controllers;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.minio.domain.resource.DownloadResource;
import org.ek.cloud_storage.minio.domain.resource.Resource;
import org.ek.cloud_storage.minio.mappers.ResourceMapper;
import org.ek.cloud_storage.minio.services.BucketService;
import org.ek.cloud_storage.minio.services.PathService;
import org.simpleframework.xml.Path;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final PathService pathService;

    private final BucketService bucketService;

    private final ResourceMapper resourceMapper;

   @GetMapping
    public ResponseEntity<ResourceResponseDTO> getResources(Principal principal,
                                                           @RequestParam String path) throws IOException {

       String fullPath = pathService.fullPathForUser(principal, path);

       Resource resource = bucketService.getResourceInfo(fullPath);

       ResourceResponseDTO resourceResponseDTO = resourceMapper.resourceToResourceResponseDTO(resource);

       return ResponseEntity.ok(resourceResponseDTO);

   }

       @DeleteMapping
    public ResponseEntity<Void> deleteResources(Principal principal,
                                                @RequestParam String path) throws IOException {

       String fullPath = pathService.fullPathForUser(principal, path);

       bucketService.deleteResource(fullPath);

       return ResponseEntity.noContent().build();
   }

   @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResources(
            Principal principal,
            @RequestParam String path) throws IOException {

       String fullPath = pathService.fullPathForUser(principal, path);

       DownloadResource downloadResource = bucketService.downloadResource(fullPath);

       return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadResource.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloadResource.getBody());

   }

       @GetMapping("/move")
    public ResponseEntity<ResourceResponseDTO> moveResources(Principal principal,
                                                             @RequestParam String from,
                                                             @RequestParam String to) throws IOException {

       String fullPathFrom = pathService.fullPathForUser(principal, from);
       String fullPathTo = pathService.fullPathForUser(principal, to);

       bucketService.moveResource(fullPathFrom, fullPathTo);

       Resource resource = bucketService.getResourceInfo(fullPathTo);

       ResourceResponseDTO resourceResponseDTO = resourceMapper.resourceToResourceResponseDTO(resource);

       return ResponseEntity.ok(resourceResponseDTO);

   }

      @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDTO>> searchResources(Principal principal,
                                                                     String query) throws IOException {

       String userFolder = pathService.getUserPrefix(principal);

       //check what wrong with frontend

       List<Resource> results = bucketService.searchResource(userFolder, query);

       List<ResourceResponseDTO> response = resourceMapper.resourceListToDtoList(results);

       return ResponseEntity.ok(response);
   }


    @PostMapping
    public ResponseEntity<ResourceResponseDTO> uploadResources(Principal principal,
                                                               @RequestParam("path") String path,
                                                               @RequestPart("object") MultipartFile file) throws IOException {

        System.out.println("upload resources controller");

        System.out.println("Path: " + path);

       String fullPath =  pathService.fullPathForUser(principal, path);

       bucketService.uploadResource(fullPath, file);

       Resource resource = bucketService.getResourceInfo(fullPath);

       ResourceResponseDTO resourceResponseDTO = resourceMapper.resourceToResourceResponseDTO(resource);

       return ResponseEntity.ok(resourceResponseDTO);

   }




}
