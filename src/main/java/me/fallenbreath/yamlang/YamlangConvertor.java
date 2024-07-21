package me.fallenbreath.yamlang;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public abstract class YamlangConvertor extends DefaultTask
{
	private static final String YAML_PREFIX = ".yml";
	private static final String JSON_PREFIX = ".json";

	private SourceSet sourceSet;

	void setSourceSet(SourceSet sourceSet)
	{
		this.sourceSet = sourceSet;
	}

	@TaskAction
	private void doConversion()
	{
		try
		{
			this.doConversionImpl();
		}
		catch (Exception e)
		{
			this.getLogger().error("Failed to execute yaml-to-json conversion", e);
			throw e;
		}
	}

	private void doConversionImpl()
	{
		YamlangExtension extension = this.getProject().getExtensions().getByType(YamlangExtension.class);
		String inputDir = extension.getInputDir().getOrElse("");
		String outputDir = extension.getOutputDir().getOrElse(inputDir);

		List<String> inputDirs = extension.getInputDirs().getOrElse(Collections.emptyList());
		Function<String, String> outputTransformer = extension.getMoveOutputDirs().getOrElse(in -> in);

		convertDirectory(extension, inputDir, outputDir);
		for (String additionalInput : inputDirs)
		{
			String destinationDir = outputTransformer.apply(additionalInput);
			convertDirectory(extension, additionalInput, destinationDir);
		}
	}

	private void convertDirectory(YamlangExtension extension, String inputDir, String outputDir)
	{
		if (inputDir.isEmpty() || outputDir.isEmpty())
		{
			return;
		}

		String targetFilePattern = extension.getTargetFilePattern().getOrElse("*" + YAML_PREFIX);
		boolean preserveYaml = extension.getPreserveYaml().getOrElse(false);

		Path basePath = Objects.requireNonNull(this.sourceSet.getOutput().getResourcesDir()).toPath();

		this.getProject().copy(copySpec -> {
			Map<String, Object> properties = new HashMap<>();
			properties.put("args", new Yamlang2JsonlangTransformer.Args(this.getLogger(), extension));

			copySpec.setFilteringCharset(extension.getCharset().getOrElse("UTF-8"));
			copySpec.from(basePath.resolve(inputDir));
			copySpec.include(targetFilePattern);
			copySpec.filter(properties, Yamlang2JsonlangTransformer.class);
			copySpec.rename(YamlangConvertor::renameYaml2Json);
			copySpec.into(basePath.resolve(outputDir));
		});
		if (!preserveYaml)
		{
			this.getProject().delete(deleteSpec -> {
				deleteSpec.delete(this.getProject().fileTree(basePath.resolve(inputDir), files -> {
					files.include(targetFilePattern);
				}));
			});
		}
	}

	private static String renameYaml2Json(String fileName)
	{
		String baseName = fileName.substring(0, fileName.length() - YAML_PREFIX.length());
		return baseName + JSON_PREFIX;
	}
}
