package me.fallenbreath.yamlang;

import org.gradle.api.tasks.SourceSet;

import java.util.List;

public class YamlangExtension
{
	/**
	 * The source set to hook up the yamlang file convertor
	 */
	public List<SourceSet> targetSourceSets;

	/**
	 * The path to the input directory containing the language files in yaml
	 * <p>
	 * The path a related path of the output resources directory of the given source set
	 */
	public String inputDir;

	/**
	 * The path to the output directory containing the language files in yaml
	 * <p>
	 * The path a related path of the output resources directory of the given source set
	 * <p>
	 * When not set, it will use {@link #inputDir} as the fallback value
	 */
	public String outputDir;

	/**
	 * The file name pattern of the language files in yaml
	 * <p>
	 * Default value: *.yml
	 */
	public String targetFilePattern;

	/**
	 * Should the original yaml language files be preserved after the conversion
	 * <p>
	 * Default value: false, which means those yaml language files will be deleted after the conversion
	 */
	public Boolean preserveYaml;
}