package me.fallenbreath.yamlang;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.api.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FilterReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Yamlang2JsonlangMapper extends FilterReader
{
	public static class Args
	{
		public final Logger logger;
		public final boolean enableOwolibRichTranslations;

		public Args(Logger logger, YamlangExtension extension)
		{
			this.logger = logger;
			this.enableOwolibRichTranslations = extension.getOwolibRichTranslations().getOrElse(false);
		}
	}

	private final Reader originalReader;
	private Args args = null;

	public Yamlang2JsonlangMapper(Reader reader)
	{
		super(reader);
		this.originalReader = reader;
	}

	// will be invoked in org.gradle.api.internal.file.copy.FilterChain#add
	@SuppressWarnings("unused")
	public void setArgs(Args args)
	{
		this.args = args;
		this.performTransformation();
	}

	private void performTransformation()
	{
		if (this.in != this.originalReader)
		{
			return;  // already transformed
		}
		Objects.requireNonNull(this.args);

		try
		{
			String input = CharStreams.toString(this.in);
			String transformed = this.yamlang2Jsonlang(input);
			this.in = new StringReader(transformed);
		}
		catch (Exception e)
		{
			this.args.logger.error("Transformation from yamlang to json failed");
			throw new RuntimeException(e);
		}
	}

	private String yamlang2Jsonlang(String yamlString)
	{
		Map<Object, Object> yamlMap = new Yaml().load(yamlString);
		Map<String, Object> result = new LinkedHashMap<>();
		this.parseMap(result, yamlMap, "");
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
				throw new IllegalArgumentException(String.format("Unexpected key type '%s' for key '%s' at path '%s'", keyObj.getClass(), keyObj, prefix));
			}

			String key = (String)keyObj;
			String fullKey = prefix.isEmpty() ? key : (!key.equals(".") ? prefix + "." + key : prefix);
			if (value instanceof String)
			{
				// translation entry
				result.put(fullKey, value);
			}
			else if (value instanceof Map)
			{
				// nested translation map
				this.parseMap(result, (Map<Object, Object>)value, fullKey);
			}
			else if (this.args.enableOwolibRichTranslations && value instanceof List)
			{
				// owo-lib rich translation, keep the list intact
				result.put(fullKey, value);
			}
			else
			{
				// unknown type
				throw new IllegalArgumentException(String.format("Unexpected value type '%s' for value '%s' on key '%s'", value.getClass(), value, fullKey));
			}
		});
	}
}
