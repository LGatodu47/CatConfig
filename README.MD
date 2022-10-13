Cat Config
=========

<img src="img/icon.png" width=256></img>

***

### What is Cat Config ?
Cat Config is a Java library with a whole config system. To implement it in your project, you first need
to obtain the artifacts located on the maven central repository or at
`https://s01.oss.sonatype.org/content/repositories/releases`.
You can find them at the domain `io.github.lgatodu47:cat-config`. Here is an example at what you would
have in your build.gradle:

    repositories {
        maven {
            url 'https://s01.oss.sonatype.org/content/repositories/releases'
        }
    }

    dependencies {
        implementation 'io.github.lgatodu47:catconfig:0.1.0'
    }

One the library is included in your project, you will need to implement the class `CatConfig` in order to
make your config class:
```
[...]
public class MyConfig extends CatConfig {
    private static final Logger LOGGER = LogManager.getLogger();

    public MyConfig(ConfigSide side, Path configDir) {
        super(side, configDir, LOGGER);
    }
    
    @Override
    protected @NotNull ConfigOptionAccess getConfigOptions() {
        return MyConfigOptions.OPTIONS;
    }
    
    @Override
    protected @NotNull String watcherThreadName() {
        return "My Config's watcher thread on " + side.sideName();
    }
}
```
You can see in this example that there is a Logger passed down in the super constructor. You can also
notice that two methods are implemented: `watcherThreadName()` corresponds to the name of the thread
(started when the `CatConfig` object is instanced) that will notify when the config file is modified.
The other method `getConfigOptions()` returns a `ConfigOptionAccess`, an object that allows accessing
your config options. The `MyConfigOptions` class defines all the config options. Here is what it may
look like based on the Javadoc of `ConfigOptionBuilder`:
```
[...]
public final class MyConfigOptions {
    private static final ConfigOptionBuilder BUILDER = ConfigOptionBuilder.create();
    public static final ConfigOptionAccess OPTIONS = BUILDER;
    
    static {
        BUILDER.onSide(~Side A~);
    }
    public static final ConfigOption<Boolean> BOOLEAN_OPTION = BUILDER.createBool("boolean_option", false);
    static {
        BUILDER.onSide(~Side B~);
    }
    public static final ConfigOption<Integer> INTEGER_OPTION = BUILDER.createInt("integer_option", null, 2, 8);
    public static final ConfigOption<Long> LONG_OPTION = BUILDER.createLong(~Side C~, "long_option", 30L);
}
```
For more details about what's happening above please **take a look at the Javadoc**.

***

### About CatConfig

Cat Config is in a **beta state**, meaning that this library may have parts changed or removed in the
future. It also means that there may be multiple bugs an improvements, and if you have some I
would really appreciate that you open an issue on the [issue tracker](https://github.com/LGatodu47/CatConfig/issues).