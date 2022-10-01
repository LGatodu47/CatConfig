package io.github.lgatodu47.catconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.*;
import java.util.Optional;

/**
 * Abstract class defining basic behaviour for a config.
 */
public abstract class CatConfig implements ConfigAccess {
    /**
     * Unique Gson Instance for serialization.
     */
    protected static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    /**
     * The lock for synchronization.
     */
    protected final Object lock = new Object();
    protected final ConfigSide side;
    protected final Logger logger;
    protected final ConfigOptionAccess options;
    protected final ConfigValueMap valueMap;
    protected final File configFile;
    protected boolean ignoreNextRead;
    protected boolean needsUpdate;

    /**
     * Basic constructor for the config object.
     *
     * @param side The side of the config.
     * @param configDir The directory where the config will be created.
     * @param logger The logger for this config object.
     */
    public CatConfig(ConfigSide side, Path configDir, Logger logger) {
        this.side = side;
        this.logger = logger;
        this.options = getConfigOptions();
        this.valueMap = createValueMap();
        Path configPath = makeConfigPath(configDir);
        new ConfigWatcher(configPath);
        this.configFile = configPath.toFile();
        readFromFile();
    }

    @Override
    public <V> void put(ConfigOption<V> option, @Nullable V value) {
        synchronized (lock) {
            valueMap.put(option, value);
            ignoreNextRead = true;
            needsUpdate = true;
        }
    }

    @Override
    public <V> Optional<V> get(ConfigOption<V> option) {
        return Optional.ofNullable(valueMap.get(option));
    }

    /**
     * Writes the config to a Json file.
     * This method is synchronised using the lock object to prevent writing and reading at the same time.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeToFile() {
        synchronized (lock) {
            // Create the config if it doesn't exist
            if(!configFile.exists()) {
                try {
                    if(configFile.getParentFile().mkdirs()) {
                        configFile.createNewFile();
                    }
                } catch (IOException e) {
                    logger.error(new FormattedMessage("Failed to create {} config file.", side.sideName()), e);
                    return;
                }
            }

            logger.info("Writing config to file...");

            try (JsonWriter writer = GSON.newJsonWriter(new FileWriter(configFile))) {
                writer.beginObject();
                valueMap.writeAll(writer);
                writer.endObject();
                // As the ConfigWatcher detects writing to file, we need to ignore the next read.
                ignoreNextRead = true;
                needsUpdate = false;
            } catch (Throwable t) {
                logger.error(new FormattedMessage("Unable to write {} config to file!", side.sideName()), t);
            }
        }
    }

    /**
     * Reads the config from the Json file.
     */
    public void readFromFile() {
        readFromFile(false);
    }

    /**
     * Reads the config from the Json file.
     * This method is synchronised using the lock object to prevent writing and reading at the same time.
     *
     * @param changesDetected If this method was called because changes were detected by the ConfigWatcher.
     */
    protected void readFromFile(boolean changesDetected) {
        synchronized (lock) {
            if(ignoreNextRead) {
                // If the config was programmatically updated we ignore reading from the file to avoid losing what's changed.
                ignoreNextRead = needsUpdate;
                return;
            }

            if(changesDetected) {
                logger.info("Detected changes in {} config file. Updating config...", side.sideName());
            }

            if(!configFile.exists()) {
                logger.info("Missing {} config file. Creating one...", side.sideName());
                writeToFile();
                return;
            }

            try (JsonReader reader = GSON.newJsonReader(new FileReader(configFile))) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    JsonToken token = reader.peek();
                    ConfigOption<?> option = this.options.get(side, name);
                    if (option == null) {
                        reader.skipValue();
                        continue;
                    }

                    if (token.equals(JsonToken.NULL)) {
                        valueMap.put(option, null);
                    } else {
                        valueMap.readAndPut(reader, option);
                    }
                }
                reader.endObject();
                // Should never be true but just in case
                needsUpdate = false;
            } catch (FileNotFoundException ignored) {
            } catch (Throwable t) {
                logger.error(new FormattedMessage("Unable to read {} config from file!", side.sideName()), t);
            }
        }
    }

    /**
     * @return The options to define to that config.
     */
    @NotNull
    protected abstract ConfigOptionAccess getConfigOptions();

    /**
     * @return A ConfigValueMap object that will store our config values.
     */
    @NotNull
    protected ConfigValueMap createValueMap() {
        return ConfigValueMap.create(this.side, this.options);
    }

    /**
     * @param configDir The config directory.
     * @return A path of the config file that will be created.
     */
    @NotNull
    protected Path makeConfigPath(Path configDir) {
        return configDir.resolve(side.sideName() + ".json");
    }

    /**
     * @return The name of the ConfigWatcher thread.
     */
    @NotNull
    protected abstract String watcherThreadName();

    /**
     * Inner class that is a thread itself and watches for changes in config.
     */
    protected class ConfigWatcher extends Thread {
        private final Path file;

        ConfigWatcher(Path file) {
            this.file = file;
            setName(watcherThreadName());
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            try(WatchService service = FileSystems.getDefault().newWatchService()) {
                file.getParent().register(service, StandardWatchEventKinds.ENTRY_MODIFY);
                // We need this field because changes get detected two times: when file content is modified and when file metadata is modified.
                // As the interval of detection of these events is very small, we can bypass it by checking the interval between the last detection and the current one.
                long lastModified = 0;
                boolean poll = true;
                while(poll) {
                    WatchKey key = service.take();
                    for(WatchEvent<?> event : key.pollEvents()) {
                        Path updated = (Path) event.context();
                        if(lastModified != 0 && updated.toFile().lastModified() - lastModified < 1000) {
                            lastModified = 0;
                        }
                        else {
                            if(event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY) && updated.equals(file.getFileName())) {
                                readFromFile(true);
                                lastModified = file.toFile().lastModified();
                            }
                        }
                    }
                    poll = key.reset();
                }
            } catch (IOException | InterruptedException | ClosedWatchServiceException e) {
                interrupt();
            }
        }
    }
}
