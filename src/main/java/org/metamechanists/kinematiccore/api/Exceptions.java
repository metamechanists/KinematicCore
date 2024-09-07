package org.metamechanists.kinematiccore.api;

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
}
