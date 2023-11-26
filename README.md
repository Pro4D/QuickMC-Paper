# QuickMC Paper

A Utility library for Minecraft Paper plugins, adding utility methods for methods such as:
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

Then, adding the actual dependency itself.
```xml
<dependencies>
    <dependency>
        <groupId>com.github.pro4d</groupId>
        <artifactId>QuickMC-Paper</artifactId>
        <version>Beta-1.3</version>
    </dependency>
</dependencies>
```

## Usage

Lastly, to utilize a majority of this library's methods, you must initialize the library, passing in an instance of your main plugin.
```java
@Override
public void onEnable() {
    QuickMC.init(this);
}
```
