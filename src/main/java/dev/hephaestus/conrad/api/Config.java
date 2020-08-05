package dev.hephaestus.conrad.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public interface Config {
    default Object get(Field field) {
        try {
            return field.get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    default void set(Field field, Object value) {
        try {
            field.set(this, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Retention(RetentionPolicy.RUNTIME) @interface SaveName {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME) @interface SaveType {
        Type value();

        enum Type {
            CLIENT,
            LEVEL
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Widget {
        String value();
    }

    class Bounds {
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Discrete {
            long min() default Long.MIN_VALUE;
            long max() default Long.MAX_VALUE;
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface Floating {
            double min() default Double.MIN_VALUE;
            double max() default Double.MAX_VALUE;
        }
    }
}
