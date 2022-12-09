package me.fallenbreath.yamlang;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class YamlangConvertor extends DefaultTask
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
		YamlangExtension extension = this.getProject().getExtensions().getByType(YamlangExtension.class);
		String inputDir = Optional.ofNullable(extension.inputDir).orElse("");
		String outputDir = Optional.ofNullable(extension.outputDir).orElse(inputDir);
		String targetFilePattern = Optional.ofNullable(extension.targetFilePattern).orElse("*" + YAML_PREFIX);
		boolean preserveYaml = Optional.ofNullable(extension.preserveYaml).orElse(false);

		if (inputDir.isEmpty() || outputDir.isEmpty())
		{
			return;
		}

		Path basePath = Objects.requireNonNull(this.sourceSet.getOutput().getResourcesDir()).toPath();

		this.getProject().copy(copySpec -> {
			copySpec.from(basePath.resolve(inputDir));
			copySpec.include(targetFilePattern);
			copySpec.filter(YamlLang2JsonMapper.class);
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
