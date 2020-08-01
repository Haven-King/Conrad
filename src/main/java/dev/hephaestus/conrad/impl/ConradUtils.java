package dev.hephaestus.conrad.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConradUtils {
	public static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
	public static final String MOD_ID = "conrad";
	public static final Logger LOG = LogManager.getLogger("Conrad");
	public static Identifier id(String... path) {
		return new Identifier(MOD_ID, String.join(".", path));
	}
}
