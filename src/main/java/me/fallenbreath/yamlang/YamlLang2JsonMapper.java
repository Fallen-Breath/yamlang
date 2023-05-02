package me.fallenbreath.yamlang;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlLang2JsonMapper extends FilterReader
{
	public YamlLang2JsonMapper(Reader reader) throws IOException
	{
		super(new StringReader(yamlLang2Json(CharStreams.toString(reader))));
	}

	private static String yamlLang2Json(String ymlContent)
	{
		Map<String, Object> yamlMap = new Yaml().load(ymlContent);
		Map<String, String> result = new LinkedHashMap<>();
		parseMap(result, yamlMap, "");
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(result);
	}

	@SuppressWarnings("unchecked")
	private static void parseMap(Map<String, String> result, Map<String, Object> yamlMap, String prefix)
	{
		yamlMap.forEach((key, value) -> {
			if (value == null)
			{
				return;
			}
			String fullKey = prefix.isEmpty() ? key : (!key.equals(".") ? prefix + "." + key : prefix);
			if (value instanceof String)
			{
				result.put(fullKey, (String) value);
			}
			else if (value instanceof Map)
			{
				parseMap(result, (Map<String, Object>)value, fullKey);
			}
			else
			{
				throw new RuntimeException(String.format("Unknown type %s in with key %s", value.getClass(), fullKey));
			}
		});
	}
}
