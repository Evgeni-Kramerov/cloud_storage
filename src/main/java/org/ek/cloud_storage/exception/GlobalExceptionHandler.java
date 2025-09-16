package org.ek.cloud_storage.exception;

import io.minio.errors.MinioException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(UserAlreadyExists e) {
        return ResponseEntity.status(HttpStatusCode.valueOf(409))
                .body(new ErrorResponseDTO("User with this username already exists"));

    }

    @ExceptionHandler(UserStorageException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserStorageException(MinioException e) {
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


    @ExceptionHandler(MinIoResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleMinIoResourceNotFoundException (MinIoResourceException e) {
         return ResponseEntity.status(HttpStatusCode.valueOf(404))
                  .body(new ErrorResponseDTO(e.getMessage()));

    }

    @ExceptionHandler(MinIoResourceAlreadyExists.class)
    public ResponseEntity<ErrorResponseDTO> handleMinIoResourceAlreadyExists (MinIoResourceAlreadyExists e) {
        return ResponseEntity.status(HttpStatusCode.valueOf(409))
                  .body(new ErrorResponseDTO(e.getMessage()));

    }

}