package org.ek.cloud_storage;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.minio.domain.dto.ErrorResponseDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootApplication
public class CloudStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudStorageApplication.class, args);
    }

    @RestControllerAdvice
    @RequiredArgsConstructor
    public static class GlobalExceptionHandler {

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatusCode.valueOf(409))
                        .body(new ErrorResponseDTO("User with this username already exists"));

        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

            String errorMessage = e.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList()
                    .getFirst();

            return ResponseEntity.status(HttpStatusCode.valueOf(400))
                        .body(new ErrorResponseDTO("Validation error " + errorMessage));

        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException e) {
            return ResponseEntity.status(HttpStatusCode.valueOf(400))
                    .body(new ErrorResponseDTO("Validation error " + e.getMessage()));

        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatusCode.valueOf(400))
                    .body(new ErrorResponseDTO("Validation error " + e.getMessage()));
        }


    }
}
