package dev.hephaestus.conrad.impl.common.serialization;

import org.jetbrains.annotations.Nullable;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.api.serialization.ValueSerializer;
import dev.hephaestus.jankson.Jankson;
import dev.hephaestus.jankson.JsonElement;
import dev.hephaestus.jankson.JsonObject;
import dev.hephaestus.jankson.JsonPrimitive;
import dev.hephaestus.jankson.api.SyntaxError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JanksonSerializer extends ConfigSerializer<JsonElement, JsonObject> {
	public static final JanksonSerializer INSTANCE = new JanksonSerializer();

	private final Jankson jankson = new Jankson();

	private JanksonSerializer() {
		this.addSerializer(Boolean.class, JsonPrimitive.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, JsonPrimitive.class, IntSerializer.INSTANCE);
		this.addSerializer(Long.class, JsonPrimitive.class, LongSerializer.INSTANCE);
		this.addSerializer(String.class, JsonPrimitive.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, JsonPrimitive.class, FloatSerializer.INSTANCE);
		this.addSerializer(Double.class, JsonPrimitive.class, DoubleSerializer.INSTANCE);
	}

	@Override
	public JsonObject start(Config config) {
		JsonObject object = new JsonObject();
//		object.put("version", new JsonPrimitive(config.version().toString()));
		return object;
	}

	@Override
	protected <R extends JsonElement> void add(JsonObject object, String key, R representation, @Nullable String comment) {
		object.put(key, representation, comment);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(JsonObject object, String key) {
		return (V) object.get(key);
	}

	@Override
	public JsonObject read(InputStream in) throws IOException {
		try {
			return this.jankson.load(in);
		} catch (SyntaxError syntaxError) {
			throw new IOException(syntaxError);
		}
	}

	@Override
	protected void write(JsonObject object, OutputStream out) throws IOException {
		out.write(object.toJson(true, true).getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String fileExtension() {
		return "json5";
	}

	private interface JanksonValueSerializer<R extends JsonElement, V> extends ValueSerializer<JsonElement, R, V> {
	}

	private static class BooleanSerializer implements JanksonValueSerializer<JsonPrimitive, Boolean> {
		static BooleanSerializer INSTANCE = new BooleanSerializer();

		@Override
		public JsonPrimitive serialize(Boolean value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Boolean deserialize(JsonElement representation) {
			return ((JsonPrimitive) representation).asBoolean(false);
		}
	}

	private static class IntSerializer implements JanksonValueSerializer<JsonPrimitive, Integer> {
		static IntSerializer INSTANCE = new IntSerializer();

		@Override
		public JsonPrimitive serialize(Integer value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Integer deserialize(JsonElement representation) {
			return ((JsonPrimitive) representation).asInt(-1);
		}
	}

	private static class LongSerializer implements JanksonValueSerializer<JsonPrimitive, Long> {
		static LongSerializer INSTANCE = new LongSerializer();

		@Override
		public JsonPrimitive serialize(Long value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Long deserialize(JsonElement representation) {
			return ((JsonPrimitive) representation).asLong(-1);
		}
	}

	private static class StringSerializer implements JanksonValueSerializer<JsonPrimitive, String> {
		static StringSerializer INSTANCE = new StringSerializer();

		@Override
		public JsonPrimitive serialize(String value) {
			return new JsonPrimitive(value);
		}

		@Override
		public String deserialize(JsonElement representation) {
			return ((JsonPrimitive) representation).asString();
		}
	}

	private static class FloatSerializer implements JanksonValueSerializer<JsonPrimitive, Float> {
		public static final FloatSerializer INSTANCE = new FloatSerializer();

		@Override
		public JsonPrimitive serialize(Float value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Float deserialize(JsonElement representation) {
			return ((JsonPrimitive) representation).asFloat(0);
		}
	}

	private static class DoubleSerializer implements JanksonValueSerializer<JsonPrimitive, Double> {
		public static final DoubleSerializer INSTANCE = new DoubleSerializer();

		@Override
		public JsonPrimitive serialize(Double value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Double deserialize(JsonElement representation) {
			return ((JsonPrimitive) representation).asDouble(0);
		}
	}
}
