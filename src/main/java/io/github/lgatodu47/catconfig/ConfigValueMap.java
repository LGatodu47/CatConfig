package io.github.lgatodu47.catconfig;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An interface describing a map that associates all the config options to a value.
 */
public interface ConfigValueMap {
    /**
     * @param configSide The side of the config.
     * @param options The defined options of the config.
     * @return A new instance of the default implementation of ConfigValueMap.
     */
    static ConfigValueMap create(ConfigSide configSide, ConfigOptionAccess options) {
        return new Impl(configSide, options);
    }

    /**
     * @param option The option of the value to get.
     * @return The value that is mapped to the given option.
     * @param <V> The type of the value.
     */
    @Nullable <V> V get(ConfigOption<V> option);

    /**
     * @param option The option to assign.
     * @param value The value that will be assigned to this option.
     * @param <V> The type of the value.
     */
    <V> void put(ConfigOption<V> option, @Nullable V value);

    /**
     * Writes all the entries stored in the map.
     * @param writer The Json writer in which the entries will be written.
     * @throws IOException If an error occurs when writing to the writer.
     */
    void writeAll(JsonWriter writer) throws IOException;

    /**
     * Passes the Json reader to the config option in order to deserialize the value which will be then stored in the map.
     * @param reader The reader from which the value will be read.
     * @param option The option that will be used to deserialize the value.
     * @throws IOException If there is an error when reading from the Json reader.
     */
    void readAndPut(JsonReader reader, ConfigOption<?> option) throws IOException;

    /**
     * Default implementation of ConfigValueMap
     */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    final class Impl implements ConfigValueMap {
        /**
         * Using a cache for indexes (as the list is not modified, only the pairs) so repeated operations execute quicker.
         */
        private final Object2IntMap<ConfigOption<?>> indexCache = new Object2IntArrayMap<>();
        private final List<ConfigValuePair<?>> values;

        private Impl(ConfigSide configSide, ConfigOptionAccess options) {
            values = new ArrayList<>(createConfigOptionsDefaultList(configSide, options));
        }

        @Override
        @Nullable
        public <V> V get(ConfigOption<V> option) {
            if (indexCache.containsKey(option)) {
                return (V) values.get(indexCache.getInt(option)).value;
            }

            for (ConfigValuePair<?> pair : values) {
                if (pair.option.equals(option)) {
                    indexCache.putIfAbsent(option, values.indexOf(pair));
                    return (V) pair.value;
                }
            }

            return null;
        }

        @Override
        public <V> void put(ConfigOption<V> option, @Nullable V value) {
            if (indexCache.containsKey(option)) {
                ((ConfigValuePair<V>) values.get(indexCache.getInt(option))).value = value;
                return;
            }

            for (ConfigValuePair<?> pair : values) {
                if (pair.option.equals(option)) {
                    indexCache.putIfAbsent(option, values.indexOf(pair));
                    ((ConfigValuePair<V>) pair).value = value;
                }
            }
        }

        @Override
        public void writeAll(JsonWriter writer) throws IOException {
            for(ConfigValuePair<?> pair : values) {
                pair.write(writer);
            }
        }

        @Override
        public void readAndPut(JsonReader reader, ConfigOption<?> option) throws IOException {
            if (indexCache.containsKey(option)) {
                ((ConfigValuePair<Object>) values.get(indexCache.getInt(option))).value = option.read(reader);
                return;
            }

            for (ConfigValuePair<?> pair : values) {
                if (pair.option.equals(option)) {
                    indexCache.putIfAbsent(option, values.indexOf(pair));
                    ((ConfigValuePair<Object>) pair).value = option.read(reader);
                }
            }
        }

        /**
         * @param configSide The side of the config options to get.
         * @param options The options defined in the config.
         * @return A list with all the options defined in the config paired with their default value.
         */
        private static List<ConfigValuePair<?>> createConfigOptionsDefaultList(ConfigSide configSide, ConfigOptionAccess options) {
            return options.options(configSide).stream().map(option -> new ConfigValuePair<>((ConfigOption<Object>) option, option.defaultValue())).collect(Collectors.toList());
        }

        private static final class ConfigValuePair<V> {
            private final ConfigOption<V> option;
            private V value;

            private ConfigValuePair(ConfigOption<V> option, @Nullable V value) {
                this.option = option;
                this.value = value;
            }

            private void write(JsonWriter writer) throws IOException {
                if (value == null) {
                    option.writeDefaultOrNull(writer);
                } else {
                    option.writeWithName(writer, value);
                }
            }
        }
    }
}
