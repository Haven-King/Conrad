package dev.hephaestus.conrad.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

public class YAMLSerializer implements Config.Serializer {
	public static Config.Serializer INSTANCE = new YAMLSerializer();

	private final ObjectMapper mapper;

	public YAMLSerializer(YAMLFactory factory) {
		this.mapper = new ObjectMapper(factory);
	}

	public YAMLSerializer() {
		this(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
	}

	@Override
	public void save(File configFile, Config config) throws IOException {
		this.mapper.writeValue(new BufferedWriter(new FileWriter(configFile)), config);
	}

	@Override
	public <T extends Config> T load(File configFile, Class<T> configClass) throws IOException {
		return this.mapper.readValue(configFile, configClass);
	}

	@Override
	public String fileType() {
		return "yaml";
	}
}
