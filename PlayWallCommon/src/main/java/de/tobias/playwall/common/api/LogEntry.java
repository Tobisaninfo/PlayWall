package de.tobias.playwall.common.api;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogEntry implements Serializable
{

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter FORMATTER =
			DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

	public enum Level
	{TRACE, DEBUG, INFO, WARN, ERROR, FATAL}

	private final long timestamp;
	private final Level level;
	private final String loggerName;
	private final String message;
	private final String throwable;
	private final String source; // Application name / identifier

	public LogEntry(long timestamp, Level level, String loggerName,
					String message, String throwable, String source)
	{
		this.timestamp = timestamp;
		this.level = level;
		this.loggerName = loggerName;
		this.message = message;
		this.throwable = throwable;
		this.source = source;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public Level getLevel()
	{
		return level;
	}

	public String getLoggerName()
	{
		return loggerName;
	}

	public String getMessage()
	{
		return message;
	}

	public String getThrowable()
	{
		return throwable;
	}

	public String getSource()
	{
		return source;
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
