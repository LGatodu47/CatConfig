package io.github.lgatodu47.catconfig;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import static io.github.lgatodu47.catconfig.Util.clamp;

/**
 * An interface used to build your config options. It is design so that you can declare them statically
 * and use them in your code freely. Here is an example at what it might look like :
 * <pre>
 *     private static final ConfigOptionBuilder BUILDER = {@link ConfigOptionBuilder#create() ConfigOptionBuilder.create()};
 *     public static final ConfigOptionAccess OPTIONS = BUILDER;
 *     static {
 *         {@link ConfigOptionBuilder#onSide(ConfigSide) BUILDER.onSide}(~Side A~);
 *     }
 *     public static final ConfigOption&#60Boolean&#62 BOOLEAN_OPTION = {@link ConfigOptionBuilder#createBool(String, Boolean) BUILDER.createBool}("boolean_option", false);
 *     static {
 *         {@link ConfigOptionBuilder#onSide(ConfigSide) BUILDER.onSide}(~Side B~);
 *     }
 *     public static final ConfigOption&#60Integer&#62 INTEGER_OPTION = {@link ConfigOptionBuilder#createInt(String, Integer, int, int) BUILDER.createInt}("integer_option", null, 2, 8);
 *     public static final ConfigOption&#60Long&#62 LONG_OPTION = {@link ConfigOptionBuilder#createLong(ConfigSide, String, Long) BUILDER.createLong}(~Side C~, "long_option", 30L);
 * </pre>
 * In the example above, we create a new ConfigOptionManager using the {@link ConfigOptionBuilder#create()} method.<br>
 * We also downcast the builder we just created to a ConfigOptionAccess instance that will allow getting our options by their name and side.<br>
 * We then set a config side, 'Side A', for options using the static block. Every option defined without a specified
 * side will automatically be assigned to that side.<br>
 * This is the case with the first option named 'BOOLEAN_OPTION'.<br>
 * After that we change the side of the builder to the 'Side B'.<br>
 * The next option, 'INTEGER_OPTION' is therefore defined for the 'Side B'.<br>
 * However, the third option named 'LONG_OPTION', is not assigned to 'Side B' but 'Side C' as it is specified
 * on the call of the method {@link ConfigOptionBuilder#createLong(ConfigSide, String, Long)}.
 */
public interface ConfigOptionBuilder extends ConfigOptionAccess {
    /**
     * Creates a new instance of the default implementation of ConfigOptionBuilder.
     * @return a freshly new created ConfigOptionBuilder.
     */
    static ConfigOptionBuilder create() {
        return new Impl();
    }

    /**
     * @return The side that is currently assigned to this builder.
     */
    @Nullable
    ConfigSide currentSide();

    /**
     * Sets the current side of this builder to the given side.
     * @param side The new side of the builder.
     */
    void onSide(@Nullable ConfigSide side);

    /**
     * @param side The config side of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @return A freshly new created boolean config option.
     */
    ConfigOption<Boolean> createBool(@NotNull ConfigSide side, String name, @Nullable Boolean defaultValue);

    /**
     * @param side The config side of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param min The minimum value (inclusive) that this option can take.
     * @param max The maximum value (also inclusive) that this option can take.
     * @return A freshly new created integer config option.
     */
    ConfigOption<Integer> createInt(@NotNull ConfigSide side, String name, @Nullable Integer defaultValue, int min, int max);

    /**
     * @param side The config side of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param min The minimum value (inclusive) that this option can take.
     * @param max The maximum value (also inclusive) that this option can take.
     * @return A freshly new created long config option.
     */
    ConfigOption<Long> createLong(@NotNull ConfigSide side, String name, @Nullable Long defaultValue, long min, long max);

    /**
     * @param side The config side of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param min The minimum value (inclusive) that this option can take.
     * @param max The maximum value (also inclusive) that this option can take.
     * @return A freshly new created double config option.
     */
    ConfigOption<Double> createDouble(@NotNull ConfigSide side, String name, @Nullable Double defaultValue, double min, double max);

    /**
     * @param side The config side of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @return A freshly new created string config option.
     */
    ConfigOption<String> createString(@NotNull ConfigSide side, String name, @Nullable String defaultValue);

    /**
     * @param side The config side of the config option to create.
     * @param name The name of the option.
     * @param enumClass The class of the enum that the option will hold.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param <E> The type of the enum.
     * @return A freshly new created enum config option.
     */
    <E extends Enum<E>> ConfigOption<E> createEnum(@NotNull ConfigSide side, String name, Class<E> enumClass, @Nullable E defaultValue);

    // delegate methods following
    default ConfigOption<Boolean> createBool(String name, @Nullable Boolean defaultValue) {
        return createBool(Objects.requireNonNull(currentSide()), name, defaultValue);
    }

    default ConfigOption<Integer> createInt(@NotNull ConfigSide side, String name, @Nullable Integer defaultValue) {
        return createInt(side, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    default ConfigOption<Integer> createInt(String name, @Nullable Integer defaultValue, int min, int max) {
        return createInt(Objects.requireNonNull(currentSide()), name, defaultValue, min, max);
    }

    default ConfigOption<Integer> createInt(String name, @Nullable Integer defaultValue) {
        return createInt(Objects.requireNonNull(currentSide()), name, defaultValue);
    }

    default ConfigOption<Long> createLong(@NotNull ConfigSide side, String name, @Nullable Long defaultValue) {
        return createLong(side, name, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    default ConfigOption<Long> createLong(String name, @Nullable Long defaultValue, long min, long max) {
        return createLong(Objects.requireNonNull(currentSide()), name, defaultValue, min, max);
    }

    default ConfigOption<Long> createLong(String name, @Nullable Long defaultValue) {
        return createLong(Objects.requireNonNull(currentSide()), name, defaultValue);
    }

    default ConfigOption<Double> createDouble(@NotNull ConfigSide side, String name, @Nullable Double defaultValue) {
        return createDouble(side, name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    default ConfigOption<Double> createDouble(String name, @Nullable Double defaultValue, double min, double max) {
        return createDouble(Objects.requireNonNull(currentSide()), name, defaultValue, min, max);
    }

    default ConfigOption<Double> createDouble(String name, @Nullable Double defaultValue) {
        return createDouble(Objects.requireNonNull(currentSide()), name, defaultValue);
    }

    default ConfigOption<String> createString(String name, @Nullable String defaultValue) {
        return createString(Objects.requireNonNull(currentSide()), name, defaultValue);
    }

    default <E extends Enum<E>> ConfigOption<E> createEnum(String name, Class<E> enumClass, @Nullable E defaultValue) {
        return createEnum(Objects.requireNonNull(currentSide()), name, enumClass, defaultValue);
    }

    /**
     * Default implementation for ConfigOptionBuilder
     */
    final class Impl implements ConfigOptionBuilder {
        /**
         * Values are stored in a Table objects, which allows row keys and column keys.
         */
        private final Table<ConfigSide, String, ConfigOption<?>> bySideAndName = HashBasedTable.create();
        @Nullable
        private ConfigSide currentSide;

        @Override
        public ConfigOption<?> get(ConfigSide side, String name) {
            return bySideAndName.get(side, name);
        }

        @Override
        public Collection<ConfigOption<?>> options(@Nullable ConfigSide side) {
            return side == null ? bySideAndName.values() : bySideAndName.row(side).values();
        }

        @Override
        public @Nullable ConfigSide currentSide() {
            return currentSide;
        }

        @Override
        public void onSide(@Nullable ConfigSide side) {
            this.currentSide = side;
        }

        @Override
        public ConfigOption<Boolean> createBool(@NotNull ConfigSide side, String name, @Nullable Boolean defaultValue) {
            return put(side, new BooleanOption(name, defaultValue));
        }

        @Override
        public ConfigOption<Integer> createInt(@NotNull ConfigSide side, String name, @Nullable Integer defaultValue, int min, int max) {
            return put(side, new IntOption(name, defaultValue, min, max));
        }

        @Override
        public ConfigOption<Long> createLong(@NotNull ConfigSide side, String name, @Nullable Long defaultValue, long min, long max) {
            return put(side, new LongOption(name, defaultValue, min, max));
        }

        @Override
        public ConfigOption<Double> createDouble(@NotNull ConfigSide side, String name, @Nullable Double defaultValue, double min, double max) {
            return put(side, new DoubleOption(name, defaultValue, min, max));
        }

        @Override
        public ConfigOption<String> createString(@NotNull ConfigSide side, String name, @Nullable String defaultValue) {
            return put(side, new StringOption(name, defaultValue));
        }

        @Override
        public <E extends Enum<E>> ConfigOption<E> createEnum(@NotNull ConfigSide side, String name, Class<E> enumClass, @Nullable E defaultValue) {
            return put(side, new EnumOption<>(name, enumClass, defaultValue));
        }

        private <O extends ConfigOption<?>> O put(ConfigSide side, O option) {
            bySideAndName.put(side, option.name(), option);
            return option;
        }

        private static class BooleanOption extends AbstractOption<Boolean> {
            private BooleanOption(String name, @Nullable Boolean defaultValue) {
                super(name, defaultValue);
            }

            @Override
            public void write(JsonWriter writer, @NotNull Boolean value) throws IOException {
                writer.value(value);
            }

            @Override
            public Boolean read(JsonReader reader) throws IOException {
                return reader.nextBoolean();
            }
        }

        private static class IntOption extends AbstractOption<Integer> {
            private final int min, max;

            private IntOption(String name, @Nullable Integer defaultValue, int min, int max) {
                super(name, defaultValue);
                this.min = min;
                this.max = max;
            }

            @Override
            public void write(JsonWriter writer, @NotNull Integer value) throws IOException {
                writer.value(value);
            }

            @Override
            public Integer read(JsonReader reader) throws IOException {
                return clamp(reader.nextInt(), min, max);
            }
        }

        private static class LongOption extends AbstractOption<Long> {
            private final long min, max;

            private LongOption(String name, @Nullable Long defaultValue, long min, long max) {
                super(name, defaultValue);
                this.min = min;
                this.max = max;
            }

            @Override
            public void write(JsonWriter writer, @NotNull Long value) throws IOException {
                writer.value(value);
            }

            @Override
            public Long read(JsonReader reader) throws IOException {
                return clamp(reader.nextLong(), min, max);
            }
        }

        private static class DoubleOption extends AbstractOption<Double> {
            private final double min, max;

            private DoubleOption(String name, @Nullable Double defaultValue, double min, double max) {
                super(name, defaultValue);
                this.min = min;
                this.max = max;
            }

            @Override
            public void write(JsonWriter writer, @NotNull Double value) throws IOException {
                writer.value(value);
            }

            @Override
            public Double read(JsonReader reader) throws IOException {
                return clamp(reader.nextDouble(), min, max);
            }
        }

        private static class StringOption extends AbstractOption<String> {
            private StringOption(String name, @Nullable String defaultValue) {
                super(name, defaultValue);
            }

            @Override
            public void write(JsonWriter writer, @NotNull String value) throws IOException {
                writer.value(value);
            }

            @Override
            public String read(JsonReader reader) throws IOException {
                return reader.nextString();
            }
        }

        private static class EnumOption<E extends Enum<E>> extends AbstractOption<E> {
            private final Class<E> enumClass;

            private EnumOption(String name, Class<E> enumClass, @Nullable E defaultValue) {
                super(name, defaultValue);
                this.enumClass = enumClass;
            }

            @Override
            public void write(JsonWriter writer, @NotNull E value) throws IOException {
                writer.value(value.name());
            }

            @Override
            public E read(JsonReader reader) throws IOException {
                return Enum.valueOf(enumClass, reader.nextString());
            }
        }

        private static abstract class AbstractOption<V> implements ConfigOption<V> {
            private final String name;
            @Nullable
            private final V defaultValue;

            private AbstractOption(String name, @Nullable V defaultValue) {
                this.name = name;
                this.defaultValue = defaultValue;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public @Nullable V defaultValue() {
                return defaultValue;
            }
        }
    }
}
