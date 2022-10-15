package io.github.lgatodu47.catconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
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
    protected final CatConfigLogger logger;
    protected final ConfigOptionAccess options;
    protected final ConfigValueMap valueMap;
    protected final Path configFilePath;
    protected final File configFile;
    @Nullable
    protected final Thread watcherThread;
    protected boolean ignoreNextRead;
    protected boolean needsUpdate;

    /**
     * Basic constructor for the config object.
     *
     * @param side The side of the config.
     * @param configName The name of the config (and more generally of the software creating it).
     * @param logger The logger for this config object.
     */
    public CatConfig(ConfigSide side, String configName, CatConfigLogger logger) {
        this.side = side;
        this.logger = logger;
        this.options = getConfigOptions();
        this.valueMap = createValueMap();
        this.configFilePath = makeConfigPath(configName);
        this.watcherThread = makeAndStartConfigWatcherThread();
        this.configFile = configFilePath.toFile();
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
                    logger.error("Failed to create {} config file.", e, side.sideName());
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
                logger.error("Unable to write {} config to file!", t, side.sideName());
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
                logger.error("Unable to read {} config from file!", t, side.sideName());
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
     * @return A path representing the directory where config files should be created.
     */
    @NotNull
    protected abstract Path getConfigDirectory();

    /**
     * @param configName The name of the config (and more generally of the software creating it).
     * @return A path of the config file that will be created.
     */
    @NotNull
    protected Path makeConfigPath(String configName) {
        return getConfigDirectory().resolve(configName + '-' + side.sideName() + ".json");
    }

    /**
     * Methods that creates the config watcher and starts it.
     * You can use this method to disable the config watcher simply by returning null.
     * @return A ConfigWatcher instance or null if watching is disabled.
     */
    @Nullable
    protected ConfigWatcher makeAndStartConfigWatcherThread() {
        return new ConfigWatcher();
    }

    /**
     * Inner class that is a thread itself and watches for changes in config.
     */
    protected class ConfigWatcher extends Thread {
        ConfigWatcher(String name) {
            setName(name);
            setDaemon(true);
            start();
        }

        ConfigWatcher() {
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            try(WatchService service = FileSystems.getDefault().newWatchService()) {
                configFilePath.getParent().register(service, StandardWatchEventKinds.ENTRY_MODIFY);
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
                            if(event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY) && updated.equals(configFilePath.getFileName())) {
                                readFromFile(true);
                                lastModified = configFile.lastModified();
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
