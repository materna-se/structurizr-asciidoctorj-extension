package io.github.stephanpirnbaum.structurizr.asciidoctorj.extension;

/**
 * Exception thrown when issues during processing of the Structurizr workspace occur.
 *
 * @author Stephan Pirnbaum
 */
public class StructurizrException extends RuntimeException {
    public StructurizrException(String message) {
        super(message);
    }

    public StructurizrException(String message, Throwable cause) {
        super(message, cause);
    }
}
