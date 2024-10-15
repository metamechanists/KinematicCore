package org.metamechanists.kinematiccore.api;

import java.util.Set;


@SuppressWarnings("EmptyClass")
public class Exceptions {
    public static class EntityTypeMismatchException extends RuntimeException {
        public EntityTypeMismatchException(String id, String actual, String expected) {
            super("The provided entity (" + actual + ") in " + id + "does not match the entity type specified in the schema (" + expected + ")");
        }
    }

    public static class MissingConstructorException extends RuntimeException {
        public MissingConstructorException(String id) {
            super(id + " must implement a constructor like public SomeKinematicEntity(StateReader reader) { ... }");
        }
    }

    public static class IdConflictException extends RuntimeException {
        public IdConflictException(String id) {
            super(id + " is already registered");
        }
    }

    public static class ValueNotFoundException extends RuntimeException {
        public ValueNotFoundException (String key, Set<String> available) {
            super("No value found at " + key + "; available values are: " + String.join(" ", available));
        }
    }

    public static class ValueWrongTypeException extends RuntimeException {
        public ValueWrongTypeException (String key, String expected, String actual) {
            super("Expected value at " + key + " to be " + expected + " but got " + actual);
        }
    }
}
