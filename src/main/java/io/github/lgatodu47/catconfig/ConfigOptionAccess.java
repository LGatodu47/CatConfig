package io.github.lgatodu47.catconfig;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Interface used to obtain the config options that are defined by their name and {@link io.github.lgatodu47.catconfig.ConfigSide}.
 */
public interface ConfigOptionAccess {
    /**
     * @param side The side that this config option is assigned to.
     * @param name The name of the config option we want to get.
     * @return The config option that is defined for the given side with the given name.
     */
    ConfigOption<?> get(ConfigSide side, String name);

    /**
     * Gathers all the options that are defined to the given side.
     * If the side is {@code null}, a collection with all the defined options is returned.
     *
     * @param side The side of the options to return. Leave {@code null} for all options.
     * @return A collection with all the options that are defined for the given side.
     */
    Collection<ConfigOption<?>> options(@Nullable ConfigSide side);
}
