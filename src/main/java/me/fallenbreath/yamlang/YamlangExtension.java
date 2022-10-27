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
	 * Should the original yaml language files be preserved after the conversion
	 * <p>
	 * Default value: false, which means those yaml language files will be deleted after the conversion
	 */
	Property<Boolean> getPreserveYaml();
}