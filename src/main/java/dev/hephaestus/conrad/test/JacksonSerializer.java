package dev.hephaestus.conrad.test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.hephaestus.conrad.impl.common.config.ConfigDefinition;
import org.jetbrains.annotations.Nullable;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.api.serialization.ValueSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JacksonSerializer extends ConfigSerializer<JsonNode, ObjectNode> {
	public static final JacksonSerializer YAML = new JacksonSerializer(YAMLFactory.builder().configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false).build().setCodec(new YAMLMapper()), "yaml");
	public static final JacksonSerializer JSON = new JacksonSerializer(JsonFactory.builder().build().setCodec(new JsonMapper()), "json");

	private final JsonFactory jsonFactory;
	private final JsonNodeFactory jsonNodeFactory;
	private final String fileExtension;

	public JacksonSerializer(JsonFactory jsonFactory, JsonNodeFactory jsonNodeFactory, String fileExtension) {
		this.jsonFactory = jsonFactory;
		this.jsonNodeFactory = jsonNodeFactory;
		this.fileExtension = fileExtension;

		this.addSerializer(Boolean.class, BooleanNode.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, IntNode.class, IntSerializer.INSTANCE);
		this.addSerializer(String.class, TextNode.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, FloatNode.class, FloatSerializer.INSTANCE);
	}

	public JacksonSerializer(JsonFactory jsonFactory, String fileExtension) {
		this(
			jsonFactory,
			JsonNodeFactory.instance,
			fileExtension
		);
	}

	@Override
	public ObjectNode start(ConfigDefinition configDefinition) {
		ObjectNode node = new ObjectNode(this.jsonNodeFactory);

		if (configDefinition.isRoot()) {
			 node.put("version", configDefinition.getVersion().toString());
		}

		return node;
	}

	@Override
	protected <R extends JsonNode> void add(ObjectNode object, String key, R representation, @Nullable String comment) {
		object.put(key, representation);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(ObjectNode object, String key) {
		return (V) object.get(key);
	}

	@Override
	public ObjectNode read(InputStream in) throws IOException {
		return this.jsonFactory.createParser(in).readValueAsTree();
	}

	@Override
	public void write(ObjectNode object, OutputStream out) throws IOException {
		this.jsonFactory.createGenerator(out).writeTree(object);
	}

	@Override
	public String fileExtension() {
		return this.fileExtension;
	}

	private interface JacksonValueSerializer<R extends JsonNode, V> extends ValueSerializer<JsonNode, R, V> {
	}

	private static class BooleanSerializer implements JacksonValueSerializer<BooleanNode, Boolean> {
		static BooleanSerializer INSTANCE = new BooleanSerializer();

		@Override
		public BooleanNode serialize(Boolean value) {
			return BooleanNode.valueOf(value);
		}

		@Override
		public Boolean deserialize(JsonNode representation) {
			return representation.asBoolean();
		}
	}

	private static class IntSerializer implements JacksonValueSerializer<IntNode, Integer> {
		static IntSerializer INSTANCE = new IntSerializer();

		@Override
		public IntNode serialize(Integer value) {
			return IntNode.valueOf(value);
		}

		@Override
		public Integer deserialize(JsonNode representation) {
			return representation.asInt();
		}
	}

	private static class StringSerializer implements JacksonValueSerializer<TextNode, String> {
		static StringSerializer INSTANCE = new StringSerializer();

		@Override
		public TextNode serialize(String value) {
			return TextNode.valueOf(value);
		}

		@Override
		public String deserialize(JsonNode representation) {
			return representation.asText();
		}
	}

	private static class FloatSerializer implements JacksonValueSerializer<FloatNode, Float> {
		public static final FloatSerializer INSTANCE = new FloatSerializer();

		@Override
		public FloatNode serialize(Float value) {
			return FloatNode.valueOf(value);
		}

		@Override
		public Float deserialize(JsonNode representation) {
			return (float) representation.asDouble();
		}
	}
}
