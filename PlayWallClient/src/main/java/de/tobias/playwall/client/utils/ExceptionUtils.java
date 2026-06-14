package de.tobias.playwall.client.utils;

import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ExceptionUtils
{
	public static String stackTraceToString(Throwable throwable)
	{
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

}
