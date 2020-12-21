package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.Color;
import dev.inkwell.conrad.api.ConfigSerializer;
import dev.inkwell.conrad.api.StronglyTypedList;
import dev.inkwell.conrad.api.ValueSerializer;
import dev.inkwell.json.*;
import dev.inkwell.json.api.SyntaxError;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JsonSerializer extends ConfigSerializer<JsonElement, JsonObject> {
	public static final JsonSerializer INSTANCE = new JsonSerializer();

	private final Jankson jankson = new Jankson();

	private JsonSerializer() {
		this.addSerializer(Boolean.class, JsonPrimitive.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, JsonPrimitive.class, IntSerializer.INSTANCE);
		this.addSerializer(Long.class, JsonPrimitive.class, LongSerializer.INSTANCE);
		this.addSerializer(String.class, JsonPrimitive.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, JsonPrimitive.class, FloatSerializer.INSTANCE);
		this.addSerializer(Double.class, JsonPrimitive.class, DoubleSerializer.INSTANCE);
		this.addSerializer(Color.class, JsonPrimitive.class, ColorSerializer.INSTANCE);
		this.addSerializer(StronglyTypedList.class, JsonArray.class, IntegerListSerializer.INSTANCE);
	}

	@Override
	public JsonObject start() {
		return new JsonObject();
	}

	@Override
	protected <R extends JsonElement> R add(JsonObject object, String key, R representation, @Nullable String comment) {
		object.put(key, representation, comment);
		return representation;
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

	private interface JsonValueSerializer<R extends JsonElement, V> extends ValueSerializer<JsonElement, R, V> {
	}

	private static class BooleanSerializer implements JsonValueSerializer<JsonPrimitive, Boolean> {
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

	private static class IntSerializer implements JsonValueSerializer<JsonPrimitive, Integer> {
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

	private static class LongSerializer implements JsonValueSerializer<JsonPrimitive, Long> {
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

	private static class StringSerializer implements JsonValueSerializer<JsonPrimitive, String> {
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

	private static class FloatSerializer implements JsonValueSerializer<JsonPrimitive, Float> {
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

	private static class DoubleSerializer implements JsonValueSerializer<JsonPrimitive, Double> {
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

	private static class ColorSerializer implements JsonValueSerializer<JsonPrimitive, Color> {
		public static final ColorSerializer INSTANCE = new ColorSerializer();

		@Override
		public JsonPrimitive serialize(Color value) {
			return new JsonPrimitive(value.value());
		}

		@Override
		public Color deserialize(JsonElement representation) {
			return Color.ofTransparent(((JsonPrimitive) representation).asInt(0));
		}
	}

	private static class IntegerListSerializer implements JsonValueSerializer<JsonArray, StronglyTypedList<Integer>> {
		public static final IntegerListSerializer INSTANCE = new IntegerListSerializer();

		@Override
		public JsonArray serialize(StronglyTypedList<Integer> value) {
			return new JsonArray(value);
		}

		@Override
		public StronglyTypedList<Integer> deserialize(JsonElement representation) {
			StronglyTypedList<Integer> list = new StronglyTypedList<>(Integer.class);

			for (JsonElement element : (JsonArray) representation) {
				list.add(((JsonPrimitive) element).asInt(0));
			}

			return list;
		}
	}

	private static class DoubleListSerializer implements JsonValueSerializer<JsonArray, StronglyTypedList<Double>> {
		public static final DoubleListSerializer INSTANCE = new DoubleListSerializer();

		@Override
		public JsonArray serialize(StronglyTypedList<Double> value) {
			return new JsonArray(value);
		}

		@Override
		public StronglyTypedList<Double> deserialize(JsonElement representation) {
			StronglyTypedList<Double> list = new StronglyTypedList<>(Double.class);

			for (JsonElement element : (JsonArray) representation) {
				list.add(((JsonPrimitive) element).asDouble(0));
			}

			return list;
		}
	}
}
