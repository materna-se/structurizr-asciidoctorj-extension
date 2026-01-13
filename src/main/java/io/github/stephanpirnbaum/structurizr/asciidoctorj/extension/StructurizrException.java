package io.github.stephanpirnbaum.structurizr.asciidoctorj.extension;

public class StructurizrException extends RuntimeException {
    public StructurizrException(String message) {
        super(message);
    }

    public StructurizrException(String message, Throwable cause) {
        super(message, cause);
    }
}
