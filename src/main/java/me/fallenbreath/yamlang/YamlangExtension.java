package me.fallenbreath.yamlang;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

import java.util.Collection;

public interface YamlangExtension
{
	/**
	 * The source set to hook up the yamlang file convertor
	 */
	Property<Collection<SourceSet>> getTargetSourceSets();

	/**
	 * The path to the input directory containing the language files in yaml
	 * <p>
	 * The path a related path of the output resources directory of the given source set
	 */
	Property<String> getInputDir();

	/**
	 * The path to the output directory containing the language files in yaml
	 * <p>
	 * The path a related path of the output resources directory of the given source set
	 * <p>
	 * When not set, it will use {@link #getInputDir} as the fallback value
	 */
	Property<String> getOutputDir();

	/**
	 * The file name pattern of the language files in yaml
	 * <p>
	 * Default value: *.yml
	 */
	Property<String> getTargetFilePattern();

	/**
	 * Should the original yaml language files be preserved after the conversion
	 * <p>
	 * Given that Minecraft uses UTF-8 to load language files, it's generally advised to just keep the default value
	 * <p>
	 * Default value: false, which means those yaml language files will be deleted after the conversion
	 */
	Property<Boolean> getPreserveYaml();

	/**
	 * The charset of the yaml files
	 * <p>
	 * Default value: "UTF-8"
	 */
	Property<String> getCharset();

	/**
	 * Enables <a href="https://docs.wispforest.io/owo/rich-translations/">OWO-LIB Rich Translations</a> in the yaml translation files
	 * <p>
	 * This option preserves lists added to the language file
	 * <p>
	 * Default value: false, which means lists are not allowed
	 */
	Property<Boolean> getOwolibRichTranslations();
}