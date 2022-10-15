package io.github.lgatodu47.catconfig;

/**
 * Utility class (package-private because too many classes are named 'Util').
 */
// In 0.2 this class with its methods aren't used. It may be useful in the future so for now I'll leave it.
@SuppressWarnings("unused")
class Util {
    /**
     * @param value The value to clamp.
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @return A value that is adjusted to be between {@code min} and {@code max}.
     */
    static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * @param value The value to clamp.
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @return A value that is adjusted to be between {@code min} and {@code max}.
     */
    static long clamp(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * @param value The value to clamp.
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @return A value that is adjusted to be between {@code min} and {@code max}.
     */
    static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}
