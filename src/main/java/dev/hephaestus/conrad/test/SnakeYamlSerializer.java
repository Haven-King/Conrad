package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.impl.common.config.ConfigDefinition;
import org.jetbrains.annotations.Nullable;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.api.serialization.ValueSerializer;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;

public class SnakeYamlSerializer extends ConfigSerializer<Object, LinkedHashMap<String, Object>> {
	public static final SnakeYamlSerializer INSTANCE = new SnakeYamlSerializer(new Yaml());

	private final Yaml yaml;

	public SnakeYamlSerializer(Yaml yaml) {
		this.yaml = yaml;

		this.addSerializer(Boolean.class, Boolean.class, new DefaultSerializer<>());
		this.addSerializer(Integer.class, Integer.class, new DefaultSerializer<>());
		this.addSerializer(String.class, String.class, new DefaultSerializer<>());
		this.addSerializer(Double.class, Double.class, new DefaultSerializer<>());
	}

	@Override
	public LinkedHashMap<String, Object> start(ConfigDefinition configDefinition) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("version", configDefinition.getVersion().toString());
		return map;
	}

	@Override
	protected <R> void add(LinkedHashMap<String, Object> object, String key, R representation, @Nullable String comment) {
		object.put(key, representation);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(LinkedHashMap<String, Object> object, String key) {
		return (V) object.get(key);
	}

	@Override
	public LinkedHashMap<String, Object> read(InputStream in) {
		LinkedHashMap<String, Object> map = this.yaml.load(in);
		return map;
	}

	@Override
	protected void write(LinkedHashMap<String, Object> object, OutputStream out) {
		this.yaml.dump(object, new OutputStreamWriter(out));
	}

	@Override
	public String fileExtension() {
		return "yaml";
	}

	private static class DefaultSerializer<T> implements ValueSerializer<T, T, T> {
		@Override
		public T serialize(T value) {
			return value;
		}

		@Override
		public T deserialize(T representation) {
			return representation;
		}
	}
}
