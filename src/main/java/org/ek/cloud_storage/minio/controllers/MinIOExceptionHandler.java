package org.ek.cloud_storage.minio.controllers;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.ErrorResponseDTO;
import org.ek.cloud_storage.minio.exception.MinIoResourceErrorException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class MinIOExceptionHandler {

    @ExceptionHandler(MinIoResourceErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleMinIoResourceNotFoundException (MinIoResourceErrorException e) {
        return ResponseEntity.status(HttpStatusCode.valueOf(404))
                .body(new ErrorResponseDTO(e.getMessage()));

    }




}