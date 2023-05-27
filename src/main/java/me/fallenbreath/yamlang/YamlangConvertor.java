package me.fallenbreath.yamlang;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.Objects;

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
		String targetFilePattern = extension.getTargetFilePattern().getOrElse("*" + YAML_PREFIX);
		boolean preserveYaml = extension.getPreserveYaml().getOrElse(false);

		if (inputDir.isEmpty() || outputDir.isEmpty())
		{
			return;
		}

		Path basePath = Objects.requireNonNull(this.sourceSet.getOutput().getResourcesDir()).toPath();

		this.getProject().copy(copySpec -> {
			copySpec.setFilteringCharset(extension.getCharset().getOrElse("UTF-8"));
			copySpec.from(basePath.resolve(inputDir));
			copySpec.include(targetFilePattern);
			copySpec.filter(Yamlang2JsonlangMapper.class);
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
