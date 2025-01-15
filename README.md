## yamlang

[![JitPack](https://jitpack.io/v/Fallen-Breath/yamlang.svg)](https://jitpack.io/#Fallen-Breath/yamlang)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/me.fallenbreath.yamlang)](https://plugins.gradle.org/plugin/me.fallenbreath.yamlang)

A gradle plugin to convert nestable yaml language file into plain json language file for Minecraft mods

### What's nestable yaml format

Here's a regular Minecraft language file in json format:

```json5
// regular json language file (comment via json5 for showcase)
{
  "mymod.message.foo": "Foo",
  "mymod.message.bar": "Bar",
  "mymod.baz": "Baz",
  "mymod.baz.oof": "OOF"
}
```

Convert that into yaml directly and we get:

```yaml
# yaml language file with plain structure like the regular json one
mymod.message.foo: Foo
mymod.message.bar: Bar
mymod.baz: Baz
mymod.baz.oof: OOF
```

Extract the common segments in the keys, and we have the nestable yaml format:

```yaml
# nestable yaml language file 1
mymod:
  message:
    foo: Foo
    bar: Bar
  baz: 
    .: Baz
    oof: OOF
```

Since the format is nest-able, not must-nest, the following content is also accepted and equivalent:

```yaml
# nestable yaml language file 2
mymod:
  message.foo: Foo
  message:
    bar: Bar
  baz: Baz
  baz.oof: OOF
```

The equivalent key of the value in a nestable yaml language file is, all keys in the path from the root to the current value joining with `.`.
The tailing `.` will be trimmed, so we can have a single `.` as the key

Advantages of language file in nestable yaml format:

- Yaml features:
  - More intuitive
  - Comment support
  - Multi-line string support
- Nestable structures:
  - Shorter line in the translation file
  - Foldable in modern editors
  - Object reference equals to the actual translation key

### Yamlang plugin

Sadly Minecraft only support loading language files written in json format, so we cannot simply throw a yaml file into our resources folder and make it work

Of course, you can mod Minecraft hard enough and make it support yaml format and even nestable yaml format, but that requires lots of work,
and you even need to bundle a yaml parsing library into your mod since Minecraft doesn't support yaml by default

Now, it's time for the yamlang plugin to save the day!

What the plugin does is very simple: it automatically converts your nestable yaml language files inside the resources folder into regular json language files on compilation,
so you can write your language files in nestable yaml format and run your mod with language files in regular json format at the same time

Since it does everything during the compilation, no extra burden is needed at runtime

### Usages

#### 1. Apply

Since version `1.3.0`, yamlang is available in the [gradle plugin portal](https://plugins.gradle.org/plugin/me.fallenbreath.yamlang), which means you can simply apply yamlang to your project in `build.gradle`:

```groovy
// build.gradle
plugins {
    id 'me.fallenbreath.yamlang' version '1.4.1'
}
```

#### 2. Configure

Basic configuration:

```groovy
// build.gradle
yamlang {
    targetSourceSets = [sourceSets.main]
    inputDir = 'assets/mymod/lang'
}
```

Full configuration with all possible entries:

```groovy
// build.gradle
yamlang {
    // A list storing source sets where resources with your language files are
    // Usually the language files are inside your main source set
    targetSourceSets = [sourceSets.main]

    // The dir path related to the resources dir
    // There should be multiple "*.yml" language files inside
    inputDir = 'assets/mymod/lang'

    // The output path of converted json language file, related to the resources dir
    // When not set, it will use inputDir as the fallback value
    outputDir = 'assets/mymod/jsonlang'

    // The file name pattern of the language files in yaml
    // You can use it, if e.g. the yaml language files ends with ".yaml" instead of ".yml", 
    // Default value: "*.yml"
    targetFilePattern = '*.yaml'
    
    // Should the original yaml language files be preserved after the conversion
    // Default value: false, which means those yaml language files will be deleted after the conversion
    preserveYaml = true
  
    // The file encoding of the language files
    // Given that Minecraft uses UTF-8 to load language files, it's generally advised to just keep the default value
    // Default value: "UTF-8"
    charset = 'UTF-8'

    // Enables OWO-LIB Rich Translations (https://docs.wispforest.io/owo/rich-translations) in the yaml translation files
    // This option preserves lists added to the language file
    // Default value: false, which means lists are not allowed
    owolibRichTranslations = false
}
```

See also: class [me.fallenbreath.yamlang.YamlangExtension](src/main/java/me/fallenbreath/yamlang/YamlangExtension.java)

#### 3. Done

That's it, everything is done

You can now write your language files in nestable yaml format freely, and the plugin will handle the rest

#### Apply yamlang from JitPack

> [!NOTE]
> Debugging / Testing only

If you need to apply yamlang from JitPack for purposes such as debugging, you need to first teach Gradle how to locate yamlang in JitPack:

```groovy
// settings.gradle
pluginManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    resolutionStrategy {
        eachPlugin {
            switch (requested.id.id) {
                case "me.fallenbreath.yamlang": {
                    useModule("com.github.Fallen-Breath:yamlang:${requested.version}")
                    break
                }
            }
        }
    }
}
```

Now, you can apply the yamlang plugin to your project using the steps provided above

## What does the plugin do

It creates tasks named `yamlangConvert<sourceSetName>Resources` and append them to the end of the process resource tasks of the given source sets. In this task, it will:

1. Read yaml files in the given path, e.g. `en_us.yml`
2. Flatten the content into plain map without nesting structures (e.g. `Map<String, String>` in java)
3. Write them into the json file, e.g. `en_us.json`
4. Delete the yaml files in the given path. They are useless now

Basically, it converts the yaml language file into the Minecraft friendly json format language file when gradle compiles your mod,
so the subsequent processes are impervious to the original format of the language files
