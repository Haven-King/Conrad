package dev.hephaestus.conrad.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.minecraft.network.PacketByteBuf;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public interface Config extends Serializable {
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

    @JsonIgnore
    default ConfigSerializer getSerializer() {
        return YAMLConfigSerializer.INSTANCE;
    }

    static void write(PacketByteBuf buf, Config config) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try {
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(config);
            objectStream.flush();

            buf.writeString(config.getClass().getName());
            buf.writeByteArray(byteStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    static Config read(PacketByteBuf buf) {
        try {
            Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(buf.readString(32767));
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buf.readByteArray());
            ObjectInput in = null;

            try {
                in = new ObjectInputStream(byteStream);
                return configClass.cast(in.readObject());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return null;
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
}
