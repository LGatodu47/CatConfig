package io.github.lgatodu47.catconfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Interface that allows access to config values by their options.
 * @see CatConfig CatConfig for implementation details.
 */
public interface ConfigAccess {
    /**
     * Assigns the given value to the given option in the internal config map.
     *
     * @param option The option that will have that value attached to it.
     * @param value The value that will correspond to that option.
     * @param <V> The type of the value to assign (should correspond to the one of option).
     */
    <V> void put(ConfigOption<V> option, @Nullable V value);

    /**
     * Gets the value assigned to the given option.
     *
     * @param option The option that is mapped to the value we want to get.
     * @return An empty Optional if the value is absent or set to {@code null}, otherwise an Optional holding the present value.
     * @param <V> The type of the value to get and of the option.
     */
    <V> Optional<V> get(ConfigOption<V> option);

    /**
     * Gets the value assigned to the given option or the default value assigned to this option depending on if the value is absent.
     *
     * @param option The option that corresponds to the value to be obtained.
     * @return The value if it is present, the default value if it's defined, or {@code null} if none of them are.
     * @param <V> Type of the option and the value that is returned.
     * @deprecated Default config values should only be used when the config is created. In other words, the implementer should
     * handle himself the case where the value is absent. This method will be removed in the next major version.
     */
    @Deprecated
    @Nullable
    default <V> V getOrDefault(ConfigOption<V> option) {
        return get(option).orElseGet(option::defaultValue);
    }

    /**
     * Gets the value returned by {@link ConfigAccess#getOrDefault(ConfigOption)} and returns the fallback value
     * if it is {@code null}.
     *
     * @param option The option that holds the value we want to get.
     * @param fallback The fallback value in case the result of {@link ConfigAccess#getOrDefault(ConfigOption)} is null.
     * @return The fallback value if the value and default value are {@code null}, otherwise returns one of them.
     * @param <V> The type of the fallback value, the return value and the option.
     * @deprecated Default config values should only be used when the config is created. In other words, the implementer should
     * handle himself the case where the value is absent. This method will be removed in the next major version.
     */
    @Deprecated
    @NotNull
    default <V> V getOrDefault(ConfigOption<V> option, @NotNull V fallback) {
        V val = getOrDefault(option);
        return val == null ? fallback : val;
    }

    /**
     * Gets the value that is internally assigned to the given option, and if it is null returns the fallback value.
     * Same as {@code get(option).orElse(fallback)}.
     *
     * @param option The option that is mapped to the value we want to get.
     * @param fallback The fallback value in case there is no value assigned to the given option.
     * @return {@code fallback} if no value is defined for the given option, otherwise the defined value.
     * @param <V> The type of the fallback value, the return value and the option.
     */
    @NotNull
    default <V> V getOrFallback(ConfigOption<V> option, @NotNull V fallback) {
        return get(option).orElse(fallback);
    }

    /**
     * Gets the value that is internally assigned to the given option, and if it is null returns the resolved fallback value.
     * Same as {@code get(option).orElseGet(fallbackSupplier)}.
     *
     * @param option The option that is mapped to the value we want to get.
     * @param fallbackSupplier The fallback value supplier in case there is no value assigned to the given option.
     * @return The resolved fallback value if no value is defined for the given option, otherwise the defined value.
     * @param <V> The type of the fallback value, the return value and the option.
     */
    default <V> V getOrFallback(ConfigOption<V> option, @NotNull Supplier<? extends V> fallbackSupplier) {
        return get(option).orElseGet(fallbackSupplier);
    }
}
