package dev.hephaestus.conrad.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.hephaestus.conrad.impl.data.YAMLSerializer;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Config extends Serializable {
    @JsonIgnore
    default Serializer getSerializer() {
        return YAMLSerializer.INSTANCE;
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

    class Entry {
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Widget {
            String value();
        }

        public static class Bounds {
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

        @Retention(RetentionPolicy.RUNTIME)
        public @interface RequiresRestart {
        }
    }

    interface Serializer {
        void save(File file, Config config) throws IOException;
        <T extends Config> T load(File file, Class<T> configClass) throws IOException;
        String fileType();
    }
}
