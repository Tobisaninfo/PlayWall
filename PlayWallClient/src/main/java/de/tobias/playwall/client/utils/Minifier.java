package de.tobias.playwall.client.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Minifier
{
	public static String minifyCss(String css)
	{
		if(css == null || css.isEmpty()) return "";

		StringBuilder sb = new StringBuilder(css.length());
		boolean inComment = false;

		int length = css.length();
		for(int i = 0; i < length; i++)
		{
			char c = css.charAt(i);

			// Skip Comments /* ... */
			if(inComment)
			{
				if(c == '*' && i + 1 < length && css.charAt(i + 1) == '/')
				{
					inComment = false;
					i++; // skip /
				}
				continue;
			}

			if(c == '/' && i + 1 < length && css.charAt(i + 1) == '*')
			{
				inComment = true;
				i++; // skip *
				continue;
			}

			// Reduce line breaks, tabs, and unnecessary whitespace to a single space
			if(c == '\n' || c == '\r' || c == '\t')
			{
				sb.append(' ');
			}
			else
			{
				sb.append(c);
			}
		}

		// Reduce multiple spaces to a single space
		return sb.toString().replaceAll(" +", " ").trim();
	}
}
