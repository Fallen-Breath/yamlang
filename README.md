## yamlang

A gradle plugin to convert nestable yaml language file into plain json language file for Minecraft mods

### What's nestable yaml format

Here's a regular Minecraft language file in json format:

```json
{
  "mymod.message.foo": "Foo",
  "mymod.message.bar": "Bar",
  "mymod.baz": "Baz",
  "mymod.baz.oof": "OOF"
}
```

Convert that into yaml directly and we get:

```yaml
mymod.message.foo: Foo
mymod.message.bar: Bar
mymod.baz: Baz
mymod.baz.oof: OOF
```

Extract the common segments in the keys, and we have the nestable yaml format:

```yaml
mymod:
  message:
    foo: Foo
    bar: Bar
  baz: 
    .: Baz
    oof: OOF
```

Since it's nest-able, not must-nest, so you can also choose to use:

```yaml
# Example 2
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
  - Cleaner
  - Comments
  - Multi-line strings
- Nestable structures:
  - Shorter translation keys
  - Foldable in modern editors

### Yamlang plugin

Sadly Minecraft only support loading language files written in json format, so we cannot simply throw a yaml file into our resources folder and make it work

Of course, you can mod Minecraft hard enough and make it support yaml format and even nestable yaml format, but that requires lots of work,
and you even need to bundle a yaml parsing library into your mod since Minecraft doesn't support yaml by default

Now, it's time for the yamlang plugin to save the day!

What the plugin does is very simple: it automatically converts your nestable yaml language files inside the resources folder into regular json language files on compilation,
so you can write your language files in nestable yaml format and run your mod with language files in regular json format at the same time

Since it does everything during the compilation, no extra burden is needed at runtime

### Example Usages

#### 1. Apply the yamlang plugin

yamlang is available in jitpack. You need to add jitpack to the repository list for gralde plugin so gradle can locate the yamlang plugin

```groovy
// settings.gradle
pluginManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Now you can apply the plugin to your project

```groovy
// build.gradle
plugins {
    id 'me.fallenbreath.yamlang' version '1.0.0'
}
```

#### 2. Configure the yamlang plugin

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
    
    // Should the original yaml language files be preserved after the conversion
    // Default value: false, which means those yaml language files will be deleted after the conversion
    preserveYaml = true
}
```

#### 3. Done

That's it, everything is done

You can now write your language files in nestable yaml format freely, and the plugin will handle the rest

## What does the plugin do

It creates tasks named `yamlangConvert<sourceSetName>Resources` and append them to the end of the process resource tasks of the given source sets. In this task, it will:

1. Read yaml files in the given path, e.g. `en_us.yml`
2. Flatten the content into plain map without nesting structures (i.e. `Map<String, String>` in java)
3. Write them into the json file, e.g. `en_us.json`

Basically it converts the yaml language file into the Minecraft friendly json format language file when gradle compiles your mod,
so the subsequent processes are impervious to the original format of the language files
