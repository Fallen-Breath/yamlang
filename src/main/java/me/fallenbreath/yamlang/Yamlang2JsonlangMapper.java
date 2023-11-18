package me.fallenbreath.yamlang;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class Yamlang2JsonlangMapper extends FilterReader
{
	public Yamlang2JsonlangMapper(Reader reader) throws IOException
	{
		super(new StringReader(yamlang2Jsonlang(CharStreams.toString(reader))));
	}

	private static String yamlang2Jsonlang(String ymlContent)
	{
		Map<Object, Object> yamlMap = new Yaml().load(ymlContent);
		Map<String, String> result = new LinkedHashMap<>();
		parseMap(result, yamlMap, "");
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(result);
	}

	@SuppressWarnings("unchecked")
	private static void parseMap(Map<String, String> result, Map<Object, Object> yamlMap, String prefix)
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
				result.put(fullKey, (String) value);
			}
			else if (value instanceof Map)
			{
				parseMap(result, (Map<Object, Object>)value, fullKey);
			}
			else
			{
				throw new IllegalArgumentException(String.format("Bad type %s for value %s on key %s", value.getClass(), value, fullKey));
			}
		});
	}
}
