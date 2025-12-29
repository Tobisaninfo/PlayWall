package de.tobias.playwall.iconoverview.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnumUsageParser
{

	public static Set<String> parse(Path sourceRoot, String enumName) throws IOException
	{
		final Set<String> usages = parseUsagesFromJavaFiles(sourceRoot, enumName);
		usages.addAll(parseUsagesFromFxmlFiles(sourceRoot));
		return usages;
	}

	private static Set<String> parseUsagesFromJavaFiles(Path sourceRoot, String enumName) throws IOException
	{
		final Set<String> usedValues = new HashSet<>();

		final Pattern pattern = Pattern.compile(enumName + "\\.([A-Z0-9_]+)");

		Files.walk(sourceRoot).filter(p -> p.toString().endsWith(".java")).forEach(p -> {
			try
			{
				final String content = Files.readString(p);
				final Matcher matcher = pattern.matcher(content);
				while(matcher.find())
				{
					usedValues.add(matcher.group(1));
				}
			}
			catch(IOException ignored)
			{
			}
		});

		return usedValues;
	}

	private static Set<String> parseUsagesFromFxmlFiles(Path sourceRoot) throws IOException
	{
		final Set<String> usedValues = new HashSet<>();

		final Pattern pattern = Pattern.compile("icon=\"([A-Z0-9_]+)\"");

		Files.walk(sourceRoot).filter(p -> p.toString().endsWith(".fxml")).forEach(p -> {
			try
			{
				final String content = Files.readString(p);
				final Matcher matcher = pattern.matcher(content);
				while(matcher.find())
				{
					usedValues.add(matcher.group(1));
				}
			}
			catch(IOException ignored)
			{
			}
		});

		return usedValues;
	}
}