package me.fallenbreath.yamlang.utils;

import java.io.IOException;
import java.io.Reader;

public class IOUtils
{
	public static String readerToString(Reader reader) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		char[] buf = new char[4096];
		int n;
		while ((n = reader.read(buf)) != -1)
		{
			builder.append(buf, 0, n);
		}
		return builder.toString();
	}
}
