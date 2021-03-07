/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.conrad.impl;

import dev.inkwell.oliver.api.ConfigDefinition;
import dev.inkwell.oliver.api.ConfigManager;
import dev.inkwell.oliver.api.ConfigPostInitializer;
import dev.inkwell.oliver.api.SyncConfigValues;
import dev.inkwell.oliver.api.data.DataType;
import dev.inkwell.oliver.api.lang.Translator;
import dev.inkwell.oliver.api.value.ValueContainer;
import dev.inkwell.oliver.api.value.ValueContainerProvider;
import dev.inkwell.oliver.api.value.ValueKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Conrad implements ConfigPostInitializer {
    private static final String FILE = "%s.%s.lang.json";
    private static final String VALUE = "  \"%s\": \"\"";
    public static void syncAndSave(ConfigDefinition<?> config) {
        ValueContainer valueContainer = ValueContainerProvider.getInstance(config.getSaveType()).getValueContainer();
        SyncConfigValues.sendConfigValues(config, valueContainer);

        if (valueContainer.getSaveDirectory() != null) {
            ConfigManager.save(config, valueContainer);
        }
    }

    @Override
    public void onConfigsLoaded() {
        boolean client = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();

        for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
            configDefinition.add(DataType.COMMENT, Translator.getComments(configDefinition.toString()));

            for (ValueKey<?> valueKey : configDefinition) {
                valueKey.add(DataType.COMMENT, Translator.getComments(valueKey.toString()));
            }

            if (client) {
                ConfigScreenProvider.register(ConfigManager.getValues(configDefinition));
            }

            if (dev) {
                Path langFile = FabricLoader.getInstance().getConfigDir().normalize().resolve(configDefinition.getPath()).resolve(
                        String.format(FILE, configDefinition.getName(), configDefinition.getSerializer().getExtension())
                );

                try {
                    Files.createDirectories(langFile.getParent());

                    List<String> keys = new ArrayList<>();

                    keys.add(configDefinition.toString());

                    String previousKey = null, previousParent = null;

                    for (ValueKey<?> valueKey : configDefinition) {
                        String key = valueKey.toString();
                        String parent = key.substring(0, key.lastIndexOf('/'));

                        if (!parent.equals(previousParent) && !parent.equals(previousKey)) {
                            keys.add(null);
                        }

                        if (!keys.contains(parent)) {
                            keys.add(parent);
                        }

                        keys.add(key);

                        previousKey = key;
                        previousParent = parent;
                    }

                    try (Writer writer = Files.newBufferedWriter(langFile)) {
                        writer.write("{\n");

                        Iterator<String> iterator = keys.iterator();

                        while (iterator.hasNext()) {
                            String key = iterator.next();

                            if (key != null) {
                                writer.write(String.format(VALUE, key));

                                if (iterator.hasNext()) {
                                    writer.write(',');
                                }
                            }

                            writer.write('\n');
                        }

                        writer.write("}");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
