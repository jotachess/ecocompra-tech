package com.ecocompra.tech.exception;

public class IllegalOperationException extends RuntimeException {

    public IllegalOperationException(String mensaje) {
        super(mensaje);
    }
}
