package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.Color;
import dev.inkwell.conrad.api.ConfigSerializer;
import dev.inkwell.conrad.api.ConfigValue;
import dev.inkwell.conrad.api.ValueSerializer;
import dev.inkwell.conrad.json.*;
import dev.inkwell.conrad.json.api.SyntaxError;
import dev.inkwell.vivid.util.Array;
import dev.inkwell.vivid.util.Table;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonSerializer extends ConfigSerializer<JsonElement, JsonObject> {
	public static final JsonSerializer INSTANCE = new JsonSerializer();

	private final Jankson jankson = new Jankson();

	private JsonSerializer() {
		this.addSerializer(Boolean.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, IntSerializer.INSTANCE);
		this.addSerializer(Long.class, LongSerializer.INSTANCE);
		this.addSerializer(String.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, FloatSerializer.INSTANCE);
		this.addSerializer(Double.class, DoubleSerializer.INSTANCE);
		this.addSerializer(Color.class, ColorSerializer.INSTANCE);
		this.addSerializer(Array.class, value -> this.arrayValueSerializer(value.getDefaultValue()));
		this.addSerializer(Table.class, value -> this.tableValueSerializer(value.getDefaultValue()));
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

	protected <V> ValueSerializer<JsonElement, ?, Array<V>> arrayValueSerializer(Array<V> defaultValue) {
		return new ArraySerializer<>(defaultValue);
	}

	protected <V> ValueSerializer<JsonElement, ?, Table<V>> tableValueSerializer(Table<V> defaultValue) {
		return new TableSerializer<>(defaultValue);
	}

	public interface JsonValueSerializer<R extends JsonElement, V> extends ValueSerializer<JsonElement, R, V> {
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

	private class ArraySerializer<T> implements JsonValueSerializer<JsonArray, Array<T>> {
		private final Array<T> defaultValue;

		private ArraySerializer(Array<T> defaultValue) {
			this.defaultValue = defaultValue;
		}

		@Override
		public JsonArray serialize(Array<T> value) {
			JsonArray array = new JsonArray();
			ValueSerializer<? extends JsonElement, ?, T> serializer = JsonSerializer.this.getSerializer(value.getDefaultValue());

			for (T t : value) {
				array.add(serializer.serialize(t));
			}

			return array;
		}

		@Override
		public Array<T> deserialize(JsonElement representation) {
			ValueSerializer<JsonElement, ?, T> serializer = JsonSerializer.this.getSerializer(this.defaultValue.getDefaultValue());

			Array<T> array = new Array<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue());

			int i = 0;
			for (JsonElement element : ((JsonArray) representation)) {
				array.addEntry();
				array.put(i++, serializer.deserialize(element));
			}

			return array;
		}
	}

	private class TableSerializer<T> implements JsonValueSerializer<JsonObject, Table<T>> {
		private final Table<T> defaultValue;

		private TableSerializer(Table<T> defaultValue) {
			this.defaultValue = defaultValue;
		}

		@Override
		public JsonObject serialize(Table<T> table) {
			JsonObject object = new JsonObject();
			ValueSerializer<? extends JsonElement, ?, T> serializer = JsonSerializer.this.getSerializer(this.defaultValue.getDefaultValue());

			for (Table.Entry<String, T>  t : table) {
				object.put(t.getKey(), serializer.serialize(t.getValue()));
			}

			return object;
		}

		@Override
		public Table<T> deserialize(JsonElement representation) {
			ValueSerializer<JsonElement, ?, T> serializer = JsonSerializer.this.getSerializer(this.defaultValue.getDefaultValue());

			Table<T> table = new Table<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue());

			int i = 0;
			for (Map.Entry<String, JsonElement> entry : ((JsonObject) representation).entrySet()) {
				table.addEntry();
				table.setKey(i, entry.getKey());
				table.put(i++, serializer.deserialize(entry.getValue()));
			}

			return table;
		}
	}
}
