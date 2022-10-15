package io.github.lgatodu47.catconfig;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that represents an array of config sides.
 * This exists to avoid arrays of config sides with null values in them and to
 * fix some issues with the {@link NotNull NotNull} and {@link org.jetbrains.annotations.Nullable Nullable} annotations.
 *
 * @since 0.2
 */
@FunctionalInterface
public interface ConfigSideArray {
    /**
     * @return A non-null array filled with non-null config sides.
     */
    @NotNull
    ConfigSide[] sides();

    /**
     * Shortcut to create a new instance of this interface.<br>
     * It allows to do this:
     * <pre>
     *     ConfigSideArray.of(MySides.SIDE_A, MySides.SIDE_B, MySides.SIDE_C)
     * </pre>
     * Instead of doing this:
     * <pre>
     *     () -&gt; new ConfigSide[] {MySides.SIDE_A, MySides.SIDE_B, MySides.SIDE_C}
     * </pre>
     * @param sides All the sides of the array (must be non-null).
     * @return A new ConfigSideArray instance.
     */
    static ConfigSideArray of(@NotNull ConfigSide... sides) {
        return () -> sides;
    }
}
