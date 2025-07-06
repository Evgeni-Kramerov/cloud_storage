package org.ek.cloud_storage.minio.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.ek.cloud_storage.minio.domain.dto.ResourceInfoResponseDTO;
import org.ek.cloud_storage.minio.services.BucketService;
import org.ek.cloud_storage.minio.services.PathService;
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

   @GetMapping
    public ResponseEntity<ResourceInfoResponseDTO> getResources(Principal principal, String path) throws IOException {

       /*
        1) Getting request
        2) Check path is valid
        3) Identify request for file or folder - in service layer
        4) Method for file or method for folder
        */



       pathService.validatePath(path);
       String fullPath = pathService.fullPathForUser(principal, path);
       return ResponseEntity.ok(bucketService.getResourceInfo(fullPath));


   }

    @PostMapping
    public ResponseEntity<ResourceInfoResponseDTO> uploadResources(Principal principal,
                                                                   @RequestPart String path,
                                                                   @RequestPart("file") MultipartFile file) throws IOException {

       String fullPath = pathService.fullPathForUser(principal, path);

       bucketService.uploadResource(fullPath, file);

       return ResponseEntity.ok(bucketService.getResourceInfo(fullPath));

   }

   @DeleteMapping
    public ResponseEntity<Void> deleteResources(Principal principal, String path) throws IOException {

       String fullPath = pathService.fullPathForUser(principal, path);

       bucketService.deleteResource(fullPath);

       return ResponseEntity.noContent().build();
   }

   @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadResources(Principal principal, String path) throws IOException {

       String fullPath = pathService.fullPathForUser(principal, path);

       StreamingResponseBody responseBody = outputStream -> {
           try(InputStream inputStream = bucketService.downloadObject(fullPath)) {
               inputStream.transferTo(outputStream);
           }
       };

       return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);

   }

   @GetMapping("/move")
    public ResponseEntity<ResourceInfoResponseDTO> moveResources(Principal principal, String from, String to) throws IOException {

       String fullPathFrom = pathService.fullPathForUser(principal, from);
       String fullPathTo = pathService.fullPathForUser(principal, to);

       bucketService.moveResource(fullPathFrom, fullPathTo);

       return ResponseEntity.ok(bucketService.getResourceInfo(fullPathTo));

   }

   @GetMapping("/search")
    public ResponseEntity<List<ResourceInfoResponseDTO>> searchResources(Principal principal,
                                                                         String query) throws IOException {

        //TODO: Add only correct user handling

       return ResponseEntity.ok(bucketService.searchResource(query));
   }



}
