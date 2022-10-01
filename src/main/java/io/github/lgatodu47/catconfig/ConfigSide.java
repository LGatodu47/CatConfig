package io.github.lgatodu47.catconfig;

/**
 * Interface that characterizes a Config Side.<br>
 * Take a look at the implementation for Minecraft of this library, CatConfigMC, to see what an
 * implementation may look like.
 */
public interface ConfigSide {
    /**
     * @return The name of this side.
     */
    String sideName();
}
