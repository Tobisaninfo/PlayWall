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
			if(start == -1)
			{
				result.append(template, pos, template.length());
				break;
			}
			result.append(template, pos, start);

			int end = template.indexOf("}", start);
			if(end == -1)
			{
				result.append(template, start, template.length());
				break;
			}

			String varName = template.substring(start + 3, end); // #name
			String replacement = values.getOrDefault(varName, "");

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
