package io.github.lgatodu47.catconfig;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * An interface used to build your config options. It is design so that you can declare them statically
 * and use them in your code freely. Here is an example at what it might look like :
 * <pre>
 *     private static final ConfigOptionBuilder BUILDER = {@link ConfigOptionBuilder#create() ConfigOptionBuilder.create()};
 *     public static final ConfigOptionAccess OPTIONS = BUILDER;
 *     static {
 *         {@link ConfigOptionBuilder#onSides(ConfigSide...) BUILDER.onSides}(MySides.SIDE_A);
 *     }
 *     public static final ConfigOption&lt;Boolean&gt; BOOLEAN_OPTION = {@link ConfigOptionBuilder#createBool(String, Boolean) BUILDER.createBool}("boolean_option", false);
 *     static {
 *         {@link ConfigOptionBuilder#onSides(ConfigSide...) BUILDER.onSides}(MySides.SIDE_B);
 *     }
 *     public static final ConfigOption&lt;Integer&gt; INTEGER_OPTION = {@link ConfigOptionBuilder#createInt(String, Integer, int, int) BUILDER.createInt}("integer_option", null, 2, 8);
 *     public static final ConfigOption&lt;Long&gt; LONG_OPTION = {@link ConfigOptionBuilder#createLong(ConfigSideArray, String, Long) BUILDER.createLong}({@link ConfigSideArray#of(ConfigSide...) ConfigSideArray.of(MySides.SIDE_C)}, "long_option", 30L);
 * </pre>
 * In the example above, we create a new ConfigOptionManager using the {@link ConfigOptionBuilder#create()} method.<br>
 * We also downcast the builder we just created to a ConfigOptionAccess instance that will allow getting our options by their name and side.<br>
 * We then set a config side, 'Side A', for options using the static block. Every option defined without a specified
 * side will automatically be assigned to that side.<br>
 * This is the case with the first option named 'BOOLEAN_OPTION'.<br>
 * After that we change the side of the builder to the 'Side B'.<br>
 * The next option, 'INTEGER_OPTION' is therefore defined for the 'Side B'.<br>
 * However, the third option named 'LONG_OPTION', is not assigned to 'Side B' but 'Side C' as it is specified
 * on the call of the method {@link ConfigOptionBuilder#createLong(ConfigSideArray, String, Long)}.<br>
 * <br>
 * <strong>NOTE</strong>: Since 0.2, you can now assign multiple sides to a builder, useful when options are sometimes defined on two sides
 * at the same time (Client and Common for example).
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
    ConfigSideArray currentSides();

    /**
     * Sets the current side of this builder to the given side.
     * @param side The new side of the builder.
     */
    void onSides(@Nullable ConfigSide... side);

    /**
     * @param sides The config sides of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @return A freshly new created boolean config option.
     */
    ConfigOption<Boolean> createBool(@NotNull ConfigSideArray sides, String name, @Nullable Boolean defaultValue);

    /**
     * @param sides The config sides of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param min The minimum value (inclusive) that this option can take.
     * @param max The maximum value (also inclusive) that this option can take.
     * @return A freshly new created integer config option.
     */
    ConfigOption<Integer> createInt(@NotNull ConfigSideArray sides, String name, @Nullable Integer defaultValue, int min, int max);

    /**
     * @param sides The config sides of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param min The minimum value (inclusive) that this option can take.
     * @param max The maximum value (also inclusive) that this option can take.
     * @return A freshly new created long config option.
     */
    ConfigOption<Long> createLong(@NotNull ConfigSideArray sides, String name, @Nullable Long defaultValue, long min, long max);

    /**
     * @param sides The config sides of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param min The minimum value (inclusive) that this option can take.
     * @param max The maximum value (also inclusive) that this option can take.
     * @return A freshly new created double config option.
     */
    ConfigOption<Double> createDouble(@NotNull ConfigSideArray sides, String name, @Nullable Double defaultValue, double min, double max);

    /**
     * @param sides The config sides of the config option to create.
     * @param name The name of the option.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @return A freshly new created string config option.
     */
    ConfigOption<String> createString(@NotNull ConfigSideArray sides, String name, @Nullable String defaultValue);

    /**
     * @param sides The config sides of the config option to create.
     * @param name The name of the option.
     * @param enumClass The class of the enum that the option will hold.
     * @param defaultValue The default value for the option. Leave to null for none.
     * @param <E> The type of the enum.
     * @return A freshly new created enum config option.
     */
    <E extends Enum<E>> ConfigOption<E> createEnum(@NotNull ConfigSideArray sides, String name, Class<E> enumClass, @Nullable E defaultValue);

    /**
     * Adds a config option to the list of config options.
     * @param sides The sides of the config option.
     * @param option The config option to add.
     * @return The config option passed previously as argument.
     * @param <O> The type of the option.
     */
    <O extends ConfigOption<?>> O put(@NotNull ConfigSideArray sides, O option);

    // delegate methods following
    default ConfigOption<Boolean> createBool(String name, @Nullable Boolean defaultValue) {
        return createBool(Objects.requireNonNull(currentSides()), name, defaultValue);
    }

    default ConfigOption<Integer> createInt(@NotNull ConfigSideArray sides, String name, @Nullable Integer defaultValue) {
        return createInt(sides, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    default ConfigOption<Integer> createInt(String name, @Nullable Integer defaultValue, int min, int max) {
        return createInt(Objects.requireNonNull(currentSides()), name, defaultValue, min, max);
    }

    default ConfigOption<Integer> createInt(String name, @Nullable Integer defaultValue) {
        return createInt(Objects.requireNonNull(currentSides()), name, defaultValue);
    }

    default ConfigOption<Long> createLong(@NotNull ConfigSideArray sides, String name, @Nullable Long defaultValue) {
        return createLong(sides, name, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    default ConfigOption<Long> createLong(String name, @Nullable Long defaultValue, long min, long max) {
        return createLong(Objects.requireNonNull(currentSides()), name, defaultValue, min, max);
    }

    default ConfigOption<Long> createLong(String name, @Nullable Long defaultValue) {
        return createLong(Objects.requireNonNull(currentSides()), name, defaultValue);
    }

    default ConfigOption<Double> createDouble(@NotNull ConfigSideArray sides, String name, @Nullable Double defaultValue) {
        return createDouble(sides, name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    default ConfigOption<Double> createDouble(String name, @Nullable Double defaultValue, double min, double max) {
        return createDouble(Objects.requireNonNull(currentSides()), name, defaultValue, min, max);
    }

    default ConfigOption<Double> createDouble(String name, @Nullable Double defaultValue) {
        return createDouble(Objects.requireNonNull(currentSides()), name, defaultValue);
    }

    default ConfigOption<String> createString(String name, @Nullable String defaultValue) {
        return createString(Objects.requireNonNull(currentSides()), name, defaultValue);
    }

    default <E extends Enum<E>> ConfigOption<E> createEnum(String name, Class<E> enumClass, @Nullable E defaultValue) {
        return createEnum(Objects.requireNonNull(currentSides()), name, enumClass, defaultValue);
    }

    default<O extends ConfigOption<?>> O put(O option) {
        return put(Objects.requireNonNull(currentSides()), option);
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
        private ConfigSideArray currentSides;

        @Override
        public ConfigOption<?> get(ConfigSide side, String name) {
            return bySideAndName.get(side, name);
        }

        @Override
        public Collection<ConfigOption<?>> options(@Nullable ConfigSide side) {
            return side == null ? bySideAndName.values() : bySideAndName.row(side).values();
        }

        @Override
        public @Nullable ConfigSideArray currentSides() {
            return currentSides;
        }

        @Override
        public void onSides(@Nullable ConfigSide... sides) {
            if(sides != null) {
                this.currentSides = ConfigSideArray.of(Arrays.stream(sides).filter(Objects::nonNull).toArray(ConfigSide[]::new));
            }
        }

        @Override
        public ConfigOption<Boolean> createBool(@NotNull ConfigSideArray sides, String name, @Nullable Boolean defaultValue) {
            return put(sides, new BooleanOption(name, defaultValue));
        }

        @Override
        public ConfigOption<Integer> createInt(@NotNull ConfigSideArray sides, String name, @Nullable Integer defaultValue, int min, int max) {
            return put(sides, new IntOption(name, defaultValue, min, max));
        }

        @Override
        public ConfigOption<Long> createLong(@NotNull ConfigSideArray sides, String name, @Nullable Long defaultValue, long min, long max) {
            return put(sides, new LongOption(name, defaultValue, min, max));
        }

        @Override
        public ConfigOption<Double> createDouble(@NotNull ConfigSideArray sides, String name, @Nullable Double defaultValue, double min, double max) {
            return put(sides, new DoubleOption(name, defaultValue, min, max));
        }

        @Override
        public ConfigOption<String> createString(@NotNull ConfigSideArray sides, String name, @Nullable String defaultValue) {
            return put(sides, new StringOption(name, defaultValue));
        }

        @Override
        public <E extends Enum<E>> ConfigOption<E> createEnum(@NotNull ConfigSideArray sides, String name, Class<E> enumClass, @Nullable E defaultValue) {
            return put(sides, new EnumOption<>(name, enumClass, defaultValue));
        }

        @Override
        public <O extends ConfigOption<?>> O put(@NotNull ConfigSideArray sides, O option) {
            for (ConfigSide side : sides.sides()) {
                bySideAndName.put(side, option.name(), option);
            }
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

        private static class IntOption extends AbstractNumberOption<Integer> {
            private IntOption(String name, @Nullable Integer defaultValue, @Nullable Integer min, @Nullable Integer max) {
                super(name, defaultValue, min, max);
            }

            @Override
            public void write(JsonWriter writer, @NotNull Integer value) throws IOException {
                writer.value(value);
            }

            @Override
            public Integer read(JsonReader reader) throws IOException {
                Integer min = min();
                Integer max = max();

                int val = reader.nextInt();
                if(min != null) {
                    val = Math.max(val, min);
                }
                if(max != null) {
                    val = Math.min(val, max);
                }
                return val;
            }
        }

        private static class LongOption extends AbstractNumberOption<Long> {
            private LongOption(String name, @Nullable Long defaultValue, @Nullable Long min, @Nullable Long max) {
                super(name, defaultValue, min, max);
            }

            @Override
            public void write(JsonWriter writer, @NotNull Long value) throws IOException {
                writer.value(value);
            }

            @Override
            public Long read(JsonReader reader) throws IOException {
                Long min = min();
                Long max = max();

                long val = reader.nextInt();
                if(min != null) {
                    val = Math.max(val, min);
                }
                if(max != null) {
                    val = Math.min(val, max);
                }
                return val;
            }
        }

        private static class DoubleOption extends AbstractNumberOption<Double> {
            private DoubleOption(String name, @Nullable Double defaultValue, @Nullable Double min, @Nullable Double max) {
                super(name, defaultValue, min, max);
            }

            @Override
            public void write(JsonWriter writer, @NotNull Double value) throws IOException {
                writer.value(value);
            }

            @Override
            public Double read(JsonReader reader) throws IOException {
                Double min = min();
                Double max = max();

                double val = reader.nextInt();
                if(min != null) {
                    val = Math.max(val, min);
                }
                if(max != null) {
                    val = Math.min(val, max);
                }
                return val;
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

        private static abstract class AbstractNumberOption<N extends Number> extends AbstractOption<N> implements ConfigOption.NumberOption<N> {
            @Nullable
            private final N min, max;

            private AbstractNumberOption(String name, @Nullable N defaultValue, @Nullable N min, @Nullable N max) {
                super(name, defaultValue);
                this.min = min;
                this.max = max;
            }

            @Override
            public @Nullable N min() {
                return min;
            }

            @Override
            public @Nullable N max() {
                return max;
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
