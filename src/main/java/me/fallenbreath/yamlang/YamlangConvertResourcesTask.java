package me.fallenbreath.yamlang;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public abstract class YamlangConvertResourcesTask extends DefaultTask
{
	private static final String YAML_PREFIX = ".yml";
	private static final String JSON_PREFIX = ".json";

	@Inject
	protected abstract FileSystemOperations getFileSystemOperations();

	@InputDirectory @PathSensitive(PathSensitivity.NAME_ONLY) @SkipWhenEmpty @IgnoreEmptyDirectories
	public abstract DirectoryProperty getInputDirectory();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Input @Optional
	public abstract Property<String> getTargetFilePattern();

	@Input @Optional
	public abstract Property<Boolean> getPreserveYaml();

	@Input @Optional
	public abstract Property<String> getCharset();

	@Input @Optional
	public abstract Property<Boolean> getOwolibRichTranslations();

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

	private FileTree getYamlFileTree() {
		return this.getInputDirectory().getAsFileTree().matching(pattern -> pattern.include(
				this.getTargetFilePattern().getOrElse("*" + YAML_PREFIX)
		));
	}

	private void doConversionImpl()
	{
		FileTree yamlFileTree = getYamlFileTree();
		DirectoryProperty outputDir = this.getOutputDirectory();

		boolean preserveYaml = this.getPreserveYaml().getOrElse(false);
		String charset = this.getCharset().getOrElse("UTF-8");
		boolean owolibRichTranslations = this.getOwolibRichTranslations().getOrElse(false);

		this.getFileSystemOperations().copy(copySpec -> {
			Map<String, Object> properties = new HashMap<>();
			properties.put("args", new Yamlang2JsonlangTransformer.Args(this.getLogger(), owolibRichTranslations));

			copySpec.setFilteringCharset(charset);
			copySpec.from(yamlFileTree);
			copySpec.filter(properties, Yamlang2JsonlangTransformer.class);
			copySpec.rename(YamlangConvertResourcesTask::renameYaml2Json);
			copySpec.into(outputDir);
		});
		if (!preserveYaml)
		{
			this.getFileSystemOperations().delete(deleteSpec -> deleteSpec.delete(yamlFileTree));
		}
	}

	private static String renameYaml2Json(String fileName)
	{
		String baseName = fileName.substring(0, fileName.length() - YAML_PREFIX.length());
		return baseName + JSON_PREFIX;
	}
}
