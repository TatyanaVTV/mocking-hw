package ru.productstar.translate;

public class MyTranslationServiceException extends RuntimeException {
    public MyTranslationServiceException(String message, Throwable ex) {
        super(message, ex);
    }
}