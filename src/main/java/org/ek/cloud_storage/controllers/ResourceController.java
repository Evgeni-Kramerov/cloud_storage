package org.ek.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.domain.dto.ResourceResponseDTO;
import org.ek.cloud_storage.domain.resource.DownloadResource;
import org.ek.cloud_storage.domain.resource.Resource;
import org.ek.cloud_storage.mappers.ResourceMapper;
import org.ek.cloud_storage.services.PathService;
import org.ek.cloud_storage.services.BucketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Slf4j
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

       log.info("Received GET request for resource for path: {}", path);

       Resource resource = bucketService.getResourceInfo(principal, path);

       ResourceResponseDTO resourceResponseDTO = resourceMapper.resourceToResourceResponseDTO(resource);

       return ResponseEntity.ok(resourceResponseDTO);

   }

       @DeleteMapping
    public ResponseEntity<Void> deleteResources(Principal principal,
                                                @RequestParam String path) throws IOException {

       log.info("Received DELETE request for resource for path: {}", path);

       bucketService.deleteResource(principal, path);

       return ResponseEntity.noContent().build();
   }

   @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResources(
            Principal principal,
            @RequestParam String path){

       log.info("Received GET request for download resource for path: {}", path);

       DownloadResource downloadResource = bucketService.downloadResource(principal, path);

       return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadResource.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloadResource.getBody());

   }

       @GetMapping("/move")
    public ResponseEntity<ResourceResponseDTO> moveResources(Principal principal,
                                                             @RequestParam String from,
                                                             @RequestParam String to) throws IOException {

       log.info("Received GET request for move resource for path: {}", from);

       Resource resource = bucketService.moveResource(principal, from, to);

       ResourceResponseDTO resourceResponseDTO = resourceMapper.resourceToResourceResponseDTO(resource);

       return ResponseEntity.ok(resourceResponseDTO);

   }

      @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDTO>> searchResources(Principal principal,
                                                                     String query) throws IOException {

       if (query == null || query.isEmpty()) {
           log.error("Received GET request for search resource for empty query");
           throw new IllegalArgumentException("query is null or empty");
       }

       log.info("Received GET request for search resource for query: {}", query);

       List<Resource> results = bucketService.searchResource(principal, query);

       List<ResourceResponseDTO> response = resourceMapper.resourceListToDtoList(results);

       return ResponseEntity.ok(response);
   }


    @PostMapping
    public ResponseEntity<List<ResourceResponseDTO>> uploadResources(Principal principal,
                                                               @RequestParam("path") String path,
                                                               @RequestParam("object") MultipartFile[] files) throws IOException {

       log.info("Received POST request for upload resource for path: {}", path);

       List<Resource> uploadedResources =  bucketService.uploadResource(principal, path, files);

       List<ResourceResponseDTO> resourceResponseDTOList = resourceMapper.resourceListToDtoList(uploadedResources);

       return new ResponseEntity<>(resourceResponseDTOList, HttpStatus.CREATED);

   }


}
