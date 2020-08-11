# <img src="https://raw.githubusercontent.com/Hephaestus-Dev/Conrad/master/Conrad.png" width=100px style="display: inline-block;"></img> Conrad
Conrad is a highly flexible config library for the [Fabric modding platform](https://fabricmc.net/).

## Setup
Add the following to the appropriate sections of your `build.gradle` file:
```groovy
repositories {
    maven {
        url "https://hephaestus.dev/release"
    }
}

dependencies {
    modImplementation "dev.hephaestus:conrad:${project.conrad_version}"
    include "dev.hephaestus:conrad:${project.conrad_version}"

    // Conrad also requires ClothConfig to build its GUI:
    // https://bintray.com/shedaniel/cloth-config-2/config-2
}
```

## Creating a Config
Here is an example config:
```java
@Config.SaveName("chelsea")
@Config.SaveType(Config.SaveType.Type.CLIENT)
public class DummyClientConfig implements Config {
	@Entry.Widget("INT_SLIDER")
	@Entry.Bounds.Discrete(min = 0, max = 9000)
	public int powerLevel = 1337;
}
```

### @Config.SaveName
This is the name of the file the config is saved to (always in a folder corresponding to the mods modid). The above config file would be saved as `.minecraft/config/conrad/chelsea.yaml`. The `@SaveName` annotation is not required, but it is **highly recommended**. If this config was not annotated with the `@SaveName` annotation it would be saved as `.minecraft/config/modid/config.yaml`. This would cause problems if your mod had multiple unannotated configs.

### @Config.SaveType
Conrad provides two ways to save your config options: `CLIENT` and `LEVEL`.

#### SaveType.CLIENT
Configs annotated with `SaveType.CLIENT` will always be saved in the root `.minecraft/config/modid/` directory. These configs are synced between the client and the server, so the server will be aware of each player's client-side config settings (while the player is connected to the server. Player configs are not saved on the server).

### SaveType.LEVEL
Configs annotated with `SaveType.LEVEL` could be saved in one of a few different places:
* In a client environment:
  * If the user modifies the configs from the title screen, they will be modifying the "defaults" for that config object. These defaults are saved in the `.minecraft/config/modid/` directory.
  * If the user is in a local world, the config values are stored in the `.minecraft/saves/WORLD_NAME/config/` directory, and are copied from the users default values in `.minecraft/config/modid/` when the world is created.
* In a dedicated server environment, they are always stored in the `server/config/modid/` directory.

`LEVEL` configs are synced from the server to operators of level 4 or above, and can be modified in the client through ModMenu. Non-operator players are not able to view `LEVEL` configs.

## Registering a Config
Registering a config is easy, and is done by adding your config objects to your `fabric.mod.json` file:
```json
  "entrypoints": {
    "main": [
      "dev.hephaestus.dummy.Dummy"
    ],
    "conrad": [
      "dev.hephaestus.dummy.DummyClientConfig",
      "dev.hephaestus.dummy.DummyServerConfig"
    ]
  }
```
The order you register configs here is the order they will appear in ModMenu.

## Accessing a Config
There are two ways to access a given config object:

### Conrad.getConfig(Class configClass)
This method returns the config object for the specified class. Used for retrieving `SaveType.LEVEL` configs and for retrieving `SaveType.CLIENT` configs on the server. Will always return a valid config object.

### Conrad.getConfig(Class configClass, ServerPlayerEntity playerEntity)
This method returns the config object for the specified class and player. Used for retrieving `SaveType.CLIENT` configs on the server where they are present.