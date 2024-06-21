package me.fallenbreath.yamlang;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.yaml.snakeyaml.Yaml;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class YamlangConvertor extends DefaultTask
{
	private static final String YAML_PREFIX = ".yml";
	private static final String JSON_PREFIX = ".json";

	private SourceSet sourceSet;
	private ConverterConfiguration configuration;

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
		this.configuration = new ConverterConfiguration(
				extension.getRichTranslations().getOrElse(false)
		);

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

	private String yamlang2Jsonlang(String ymlContent)
	{
		Map<Object, Object> yamlMap = new Yaml().load(ymlContent);
		Map<String, Object> result = new LinkedHashMap<>();
		parseMap(result, yamlMap, "");
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(result);
	}

	@SuppressWarnings("unchecked")
	private void parseMap(Map<String, Object> result, Map<Object, Object> yamlMap, String prefix)
	{
		yamlMap.forEach((keyObj, value) -> {
			if (value == null)
			{
				return;
			}
			if (!(keyObj instanceof String))
			{
				throw new IllegalArgumentException(String.format("Bad type %s for key %s at path %s", keyObj.getClass(), keyObj, prefix));
			}

			String key = (String)keyObj;
			String fullKey = prefix.isEmpty() ? key : (!key.equals(".") ? prefix + "." + key : prefix);
			if (value instanceof String)
			{
				result.put(fullKey, value);
			}
			else if (value instanceof Map)
			{
				parseMap(result, (Map<Object, Object>)value, fullKey);
			}
			else if (configuration.allowLists && value instanceof List)
			{
				result.put(fullKey, value);
			}
			else
			{
				throw new IllegalArgumentException(String.format("Bad type %s for value %s on key %s", value.getClass(), value, fullKey));
			}
		});
	}

	public class Yamlang2JsonlangMapper extends FilterReader
	{
		public Yamlang2JsonlangMapper(Reader reader) throws IOException {
			super(new StringReader(yamlang2Jsonlang(CharStreams.toString(reader))));
		}
	}
}
