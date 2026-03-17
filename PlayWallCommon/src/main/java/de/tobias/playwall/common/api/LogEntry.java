package de.tobias.playwall.common.api;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record LogEntry(long timestamp, Level level, String loggerName, String message, String throwable,
					   String source) implements Serializable
{
	@Serial
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

	public enum Level
	{
		TRACE,
		DEBUG,
		INFO,
		WARN,
		ERROR,
		FATAL
	}

	public String getFormattedTime()
	{
		return FORMATTER.format(Instant.ofEpochMilli(timestamp));
	}

	public String getShortLoggerName()
	{
		int idx = loggerName.lastIndexOf('.');
		return idx >= 0 ? loggerName.substring(idx + 1) : loggerName;
	}
}
