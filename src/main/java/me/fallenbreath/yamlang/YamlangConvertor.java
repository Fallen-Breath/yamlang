package me.fallenbreath.yamlang;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class YamlangConvertor extends DefaultTask
{
	private static final String YAML_SUFFIX = ".yml";
	private static final String JSON_SUFFIX = ".json";

	private SourceSet sourceSet;

	void setSourceSet(SourceSet sourceSet)
	{
		this.sourceSet = sourceSet;
	}

	@TaskAction
	public void doConversion()
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
		String targetFilePattern = extension.getTargetFilePattern().getOrElse("*" + YAML_SUFFIX);
		boolean preserveYaml = extension.getPreserveYaml().getOrElse(false);
		Path basePath = Objects.requireNonNull(this.sourceSet.getOutput().getResourcesDir()).toPath();

		Map<String, String> directoriesMapped;

		if (extension.getInputDir().isPresent()) {
			String inputDir = extension.getInputDir().get();
			String outputDir = extension.getOutputDir().getOrElse(inputDir);
			directoriesMapped = new HashMap<>();
			directoriesMapped.put(inputDir, outputDir);
		} else {
			directoriesMapped = extension.getDirectoriesMapped().getOrElse(new HashMap<>());
			for (String dir : extension.getDirectories().getOrElse(new ArrayList<>())) {
				directoriesMapped.put(dir, dir);
			}
		}

		for (Map.Entry<String, String> kv : directoriesMapped.entrySet())
		{
			String inputDir = kv.getKey();
			String outputDir = kv.getValue();

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
	}

	private static String renameYaml2Json(String fileName)
	{
		String baseName = fileName.substring(0, fileName.length() - YAML_SUFFIX.length());
		return baseName + JSON_SUFFIX;
	}
}
