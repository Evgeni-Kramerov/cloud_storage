package org.ek.cloud_storage.minio.exception;

public class MinIoResourceAlreadyExists extends RuntimeException {
    public MinIoResourceAlreadyExists(String message) {
        super(message);
    }
}
