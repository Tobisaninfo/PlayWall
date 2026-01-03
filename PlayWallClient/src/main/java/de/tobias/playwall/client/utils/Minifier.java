package de.tobias.playwall.client.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Minifier
{
	private static final String COMMENT_START = "/*";
	private static final String COMMENT_END = "*/";

	@SuppressWarnings("java:S127")
	public static String minifyCss(String css)
	{
		if(css == null || css.isEmpty())
		{
			return "";
		}

		final StringBuilder sb = new StringBuilder(css.length());
		int length = css.length();
		for(int i = 0; i < length; i++)
		{
			char c = css.charAt(i);

			// Handle Comments
			if(c == '/' && i + 1 < length && css.charAt(i + 1) == '*')
			{
				i = skipComments(css, i);
				continue;
			}

			// Normalize whitespace
			boolean isWhitespace = c == '\n' || c == '\r' || c == '\t';
			sb.append(isWhitespace ? ' ' : c);
		}

		return sb.toString().replaceAll(" +", " ").trim();
	}

	private static int skipComments(String css, int startIndex)
	{
		int endCommentIndex = css.indexOf(COMMENT_END, startIndex + COMMENT_START.length());
		if(endCommentIndex == -1)
		{
			return css.length();
		}
		return endCommentIndex + COMMENT_END.length() - 1;
	}
}
