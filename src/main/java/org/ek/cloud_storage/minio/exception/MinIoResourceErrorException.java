package org.ek.cloud_storage.minio.exception;

public class MinIoResourceErrorException extends RuntimeException {
    public MinIoResourceErrorException(String message) {
        super(message);
    }
}
