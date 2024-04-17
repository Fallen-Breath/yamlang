package me.fallenbreath.yamlang.utils;

public class StringUtils
{
	/**
	 * No need to handle those codepoint things,
	 * cuz this method is only used to capitalize a gradle source set name
	 */
	public static String capitalize(final String str)
	{
		if (str == null || str.isEmpty())
		{
			return str;
		}

		char firstChar = str.charAt(0);
		if (firstChar > 0x7F /* unicode? */ || Character.isUpperCase(firstChar))
		{
			return str;
		}
		else
		{
			return Character.toUpperCase(firstChar) + str.substring(1);
		}
	}
}
