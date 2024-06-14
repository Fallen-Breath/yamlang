package me.fallenbreath.yamlang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Yamlang2JsonlangMapper extends BaseParamFilterReader
{
	private boolean allowLists = false;

	public Yamlang2JsonlangMapper(Reader reader) throws IOException
	{
		super(new DelegatedStringReader(reader));
	}

	@Override
	public int read() throws IOException
	{
		if (!getInitialized())
		{
			initialize();
		}
		return super.read();
	}

	private void initialize() {
		Parameter[] params = getParameters();
		if (params != null) {
			for (Parameter param : params)
			{
				if (param.getName().equals("allowLists"))
				{
					allowLists = Boolean.parseBoolean(param.getValue());
				}
			}
		}
		DelegatedStringReader delegate = (DelegatedStringReader) in;
		String processed = yamlang2Jsonlang(delegate.getContents());
		delegate.setContents(processed);
		setInitialized(true);
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
            else if (allowLists && value instanceof List)
            {
                result.put(fullKey, value);
            }
			else
			{
				throw new IllegalArgumentException(String.format("Bad type %s for value %s on key %s", value.getClass(), value, fullKey));
			}
		});
	}
}
