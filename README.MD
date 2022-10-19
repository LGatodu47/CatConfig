Cat Config
=========

<img src="img/icon.png" width=256></img>

***

### What is Cat Config ?
Cat Config is a Java library with a whole config system. To implement it in your project, you first need
to obtain the artifacts located on the maven central repository or at
`https://s01.oss.sonatype.org/content/repositories/releases`.
You can find them at the domain `io.github.lgatodu47:catconfig`. Here is an example at what you would
have in your build.gradle:

    repositories {
        maven {
            url 'https://s01.oss.sonatype.org/content/repositories/releases'
        }
    }

    dependencies {
        implementation 'io.github.lgatodu47:catconfig:0.2.3'
    }

One the library is included in your project, you will need to implement the class `CatConfig` in order to
make your config class:
```
[...]
public class MyConfig extends CatConfig {
    public MyConfig(ConfigSide side) {
        super(side, "my-config", CatConfigLogger.named("My Config"));
    }
    
    @Override
    protected @NotNull ConfigOptionAccess getConfigOptions() {
        return MyConfigOptions.OPTIONS;
    }
    
    @Override
    protected @NotNull Path getConfigDirectory() {
        return Paths.get(...);
    }
}
```
In this example, we specify a config side in the constructor. We pass down a name for our config
('my-config') and a default instance of CatConfigLogger named "My Config". You can also
notice that two methods are implemented: `getConfigDirectory()` corresponds to the path leading to the
directory where the config files will be created and `getConfigOptions()` which returns a
`ConfigOptionAccess`, an object that allows accessing your config options.
The `MyConfigOptions` class defines all the config options. Here is what it may
look like based on the Javadoc of `ConfigOptionBuilder`:
```
[...]
public final class MyConfigOptions {
    private static final ConfigOptionBuilder BUILDER = ConfigOptionBuilder.create();
    public static final ConfigOptionAccess OPTIONS = BUILDER;
    
    static {
        BUILDER.onSides(MySides.SIDE_A);
    }
    public static final ConfigOption<Boolean> BOOLEAN_OPTION = BUILDER.createBool("boolean_option", false);
    static {
        BUILDER.onSides(MySides.SIDE_B);
    }
    public static final ConfigOption<Integer> INTEGER_OPTION = BUILDER.createInt("integer_option", null, 2, 8);
    public static final ConfigOption<Long> LONG_OPTION = BUILDER.createLong(ConfigSideArray.of(MySides.SIDE_C), "long_option", 30L);
}
```
For more details about what's happening above please **take a look at the Javadoc**.

***

### About CatConfig

Cat Config is in a **beta state**, meaning that this library may have parts changed or removed in the
future. It also means that there may be multiple bugs an improvements, and if you have some I
would really appreciate that you open an issue on the [issue tracker](https://github.com/LGatodu47/CatConfig/issues).