# QuickMC Paper

A Utility library for Minecraft Paper based plugins, adding utility methods for methods such as:
- Advanced damaging of entities
- Advanced attribute modification
- Message Coloring
- Per Player Glowing
- And More!


### Installation

You can utilize this library by importing it via Maven.
First, add the Jitpack repository.
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Next, add the actual dependency itself. Latest version number displayed below:

![GitHub Release](https://img.shields.io/github/v/release/Pro4D/QuickMC-Paper?sort=date&display_name=tag)
 = VERSION

```xml
<dependencies>
    <dependency>
        <groupId>com.github.pro4d</groupId>
        <artifactId>QuickMC-Paper</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

Lastly, use the maven-shade plugin to shade QuickMC into your project. It is recommended to relocate the library
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <relocations>
            <relocation>
                <pattern>com.pro4d.quickmc</pattern>
                <shadedPattern>*your package*.libs.quickmc</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Usage

The library must be injected into the plugin onLoad, initialized onEnable, and disabled on onDisable

```java
@Override
public void onLoad() {
 QuickMC.injectOnLoad(this);
}

@Override
public void onEnable() {
    QuickMC.init(this);
}

@Override
public void onDisable() {
 QuickMC.executeOnDisable();
}
```
