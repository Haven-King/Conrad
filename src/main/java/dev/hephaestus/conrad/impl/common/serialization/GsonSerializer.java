package dev.hephaestus.conrad.impl.common.serialization;

import com.google.gson.*;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.api.serialization.ValueSerializer;
import net.minecraft.util.JsonHelper;

import java.io.*;

public class GsonSerializer extends ConfigSerializer<JsonElement, JsonObject> {
	public static final GsonSerializer INSTANCE = new GsonSerializer();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public GsonSerializer() {
		this.addSerializer(Boolean.class, JsonPrimitive.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, JsonPrimitive.class, IntSerializer.INSTANCE);
		this.addSerializer(String.class, JsonPrimitive.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, JsonPrimitive.class, FloatSerializer.INSTANCE);
	}

	@Override
	public JsonObject start(Config config) {
		JsonObject object = new JsonObject();
		object.addProperty("version", config.version().toString());
		return object;
	}

	@Override
	protected <R extends JsonElement> void add(JsonObject object, String key, R representation) {
		object.add(key, representation);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(JsonObject object, String key) {
		return (V) object.get(key);
	}

	@Override
	public JsonObject read(InputStream in) {
		return JsonHelper.deserialize(new InputStreamReader(in));
	}

	@Override
	protected void write(JsonObject object, OutputStream out) throws IOException {
		Writer writer = new OutputStreamWriter(out);
		GSON.toJson(object, writer);
		writer.flush();
		writer.close();
	}

	@Override
	public String fileExtension() {
		return "json";
	}

	private interface GsonValueSerializer<R extends JsonElement, V> extends ValueSerializer<JsonElement, R, V> {
	}

	private static class BooleanSerializer implements GsonValueSerializer<JsonPrimitive, Boolean> {
		static BooleanSerializer INSTANCE = new BooleanSerializer();

		@Override
		public JsonPrimitive serialize(Boolean value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Boolean deserialize(JsonElement representation) {
			return representation.getAsBoolean();
		}
	}

	private static class IntSerializer implements GsonValueSerializer<JsonPrimitive, Integer> {
		static IntSerializer INSTANCE = new IntSerializer();

		@Override
		public JsonPrimitive serialize(Integer value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Integer deserialize(JsonElement representation) {
			return representation.getAsInt();
		}
	}

	private static class StringSerializer implements GsonValueSerializer<JsonPrimitive, String> {
		static StringSerializer INSTANCE = new StringSerializer();

		@Override
		public JsonPrimitive serialize(String value) {
			return new JsonPrimitive(value);
		}

		@Override
		public String deserialize(JsonElement representation) {
			return representation.getAsString();
		}
	}

	private static class FloatSerializer implements GsonValueSerializer<JsonPrimitive, Float> {
		public static final FloatSerializer INSTANCE = new FloatSerializer();

		@Override
		public JsonPrimitive serialize(Float value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Float deserialize(JsonElement representation) {
			return representation.getAsFloat();
		}
	}
}
