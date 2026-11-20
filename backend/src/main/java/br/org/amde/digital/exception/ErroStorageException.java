package br.org.amde.digital.exception;

public class ErroStorageException extends RuntimeException {

    public ErroStorageException(String message) {
        super(message);
    }

    public ErroStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

