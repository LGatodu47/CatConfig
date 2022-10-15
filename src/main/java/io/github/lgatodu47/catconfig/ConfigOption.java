package io.github.lgatodu47.catconfig;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Represents an option that is part of a config.
 * @see ConfigOptionBuilder ConfigOptionBuilder for more info about how to create config options.
 * @param <V> The type of the value mapped to this option.
 */
public interface ConfigOption<V> {
    /**
     * @return The name that will describe the option in the json file.
     */
    String name();

    /**
     * @return A default value for that option (if none is defined it returns {@code null}).
     */
    @Nullable
    V defaultValue();

    /**
     * Writes given value using the given json writer. As {@link V} can be any type of object, it is necessary
     * that a value of that type can be serialized.
     *
     * @param writer The Json writer used to write the value.
     * @param value The value to write.
     * @throws IOException If there is an error while writing to the Json writer.
     */
    void write(JsonWriter writer, @NotNull V value) throws IOException;

    /**
     * Reads a value from the given Json reader. Here again, we need this method because {@link V} can be any type
     * of object, and requires to be deserialized properly.
     *
     * @param reader The Json reader used to read the value.
     * @return A value of type {@link V} that was deserialized using the Json reader.
     * @throws IOException If there is an error while reading on the Json reader.
     */
    V read(JsonReader reader) throws IOException;

    /**
     * Writes the default value in the writer, or {@code null} if there is none.
     *
     * @param writer The Json writer in which the value is written.
     * @throws IOException If there is an error while writing to the Json writer.
     */
    default void writeDefaultOrNull(JsonWriter writer) throws IOException {
        V val = defaultValue();
        if (val == null) {
            writer.name(name());
            writer.nullValue();
        } else {
            writeWithName(writer, val);
        }
    }

    /**
     * Writes the name of this option followed by the given value to the writer.
     *
     * @param writer The Json writer in which the name and the value will be written.
     * @param value The value to write.
     * @throws IOException If there is an error when writing to the Json writer.
     */
    default void writeWithName(JsonWriter writer, @NotNull V value) throws IOException {
        writer.name(name());
        write(writer, value);
    }

    /**
     * A type of ConfigOption that holds a number. The two methods that this interface
     * declares may be useful when dealing with config options that map numbers.
     * @since 0.2
     * @param <N> The type of number that this option holds.
     */
    interface NumberOption<N extends Number> extends ConfigOption<N> {
        /**
         * @return The minimum value that the value of this option can have. {@code null} for none.
         */
        @Nullable
        N min();

        /**
         * @return The maximum value that the value of this option can have. {@code null} for none.
         */
        @Nullable
        N max();
    }
}
