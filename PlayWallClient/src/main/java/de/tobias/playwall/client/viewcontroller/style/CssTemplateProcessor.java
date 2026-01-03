package de.tobias.playwall.client.viewcontroller.style;

import java.util.Map;

public class CssTemplateProcessor
{
	private final String template;

	public CssTemplateProcessor(String template)
	{
		if(template == null)
		{
			throw new IllegalArgumentException("Template must not be null");
		}
		this.template = template;
	}

	public String render(Map<String, String> values)
	{
		StringBuilder result = new StringBuilder(template.length());

		int pos = 0;
		while(pos < template.length())
		{
			int start = template.indexOf("${#", pos);
			int end = template.indexOf("}", start);
			if(start == -1 || end == -1)
			{
				result.append(template, pos, template.length());
				break;
			}
			result.append(template, pos, start);

			final String variableName = template.substring(start + 3, end);
			final String replacement = values.getOrDefault(variableName, "");

			result.append(replacement);
			pos = end + 1;
		}

		return result.toString();
	}

	public static String render(String template, Map<String, String> values)
	{
		return new CssTemplateProcessor(template).render(values);
	}
}
