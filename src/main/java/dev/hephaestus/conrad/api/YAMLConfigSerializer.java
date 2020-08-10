package dev.hephaestus.conrad.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.*;

public class YAMLConfigSerializer implements ConfigSerializer {
	public static ConfigSerializer INSTANCE = new YAMLConfigSerializer();

	private final ObjectMapper mapper;

	public YAMLConfigSerializer(YAMLFactory factory) {
		this.mapper = new ObjectMapper(factory);
	}

	public YAMLConfigSerializer() {
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
