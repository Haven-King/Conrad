package dev.hephaestus.conrad.api;

import java.io.File;
import java.io.IOException;

public interface ConfigSerializer {
	void save(File file, Config config) throws IOException;
	<T extends Config> T load(File file, Class<T> configClass) throws IOException;
	String fileType();
}