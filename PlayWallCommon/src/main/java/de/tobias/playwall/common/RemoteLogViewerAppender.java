package de.tobias.playwall.common;

import de.tobias.playwall.common.api.LogEntry;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Log4j2 Custom Appender: Sends LogEntry objects via TCP to the central LogViewer.
 * <p>
 * Usage in log4j2.xml:
 * <RemoteLogViewerAppender name="Remote" host="localhost" port="4712" appName="MyApp"/>
 */
@Plugin(name = "RemoteLogViewerAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class RemoteLogViewerAppender extends AbstractAppender
{
	private final String host;
	private final int port;
	private final String appName;

	private ObjectOutputStream oos;
	private Socket socket;
	private final BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>(5000);
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		final Thread t = new Thread(r, "remote-log-appender");
		t.setDaemon(true);
		return t;
	});

	@SuppressWarnings("java:S107")
	protected RemoteLogViewerAppender(String name, Filter filter, Layout<? extends Serializable> layout,
									  boolean ignoreExceptions, Property[] properties,
									  String host, int port, String appName)
	{
		super(name, filter, layout, ignoreExceptions, properties);
		this.host = host;
		this.port = port;
		this.appName = appName;
	}

	@PluginFactory
	public static RemoteLogViewerAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginAttribute(value = "host", defaultString = "localhost") String host,
			@PluginAttribute(value = "port", defaultInt = 4712) int port,
			@PluginAttribute(value = "appName", defaultString = "Unknown") String appName,
			@PluginElement("Filter") Filter filter)
	{

		if(name == null)
		{
			LOGGER.error("RemoteLogViewerAppender: No name provided.");
			return null;
		}
		return new RemoteLogViewerAppender(name, filter,
				PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY,
				host, port, appName);
	}

	@Override
	public void start()
	{
		super.start();
		scheduler.scheduleWithFixedDelay(this::trySend, 0, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	@SuppressWarnings("java:S899")
	public void append(LogEvent event)
	{
		LogEntry.Level level;
		try
		{
			level = LogEntry.Level.valueOf(event.getLevel().name());
		}
		catch(IllegalArgumentException _)
		{
			level = LogEntry.Level.DEBUG;
		}

		String throwableStr = null;
		if(event.getThrown() != null)
		{
			StringWriter sw = new StringWriter();
			event.getThrown().printStackTrace(new PrintWriter(sw));
			throwableStr = sw.toString();
		}

		final LogEntry entry = new LogEntry(
				event.getTimeMillis(),
				level,
				event.getLoggerName(),
				event.getSource().getMethodName(),
				event.getSource().getLineNumber(),
				event.getMessage().getFormattedMessage(),
				throwableStr,
				appName
		);
		queue.offer(entry); // non-blocking; drop if full
	}

	private void trySend()
	{
		if(queue.isEmpty()) return;
		try
		{
			ensureConnected();
			LogEntry entry;
			while((entry = queue.poll()) != null)
			{
				oos.writeObject(entry);
			}
			oos.flush();
			oos.reset();
		}
		catch(Exception _)
		{
			closeConnection();
		}
	}

	private void ensureConnected() throws IOException
	{
		if(socket == null || socket.isClosed())
		{
			socket = new Socket(host, port);
			socket.setTcpNoDelay(true);
			oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			oos.flush();
		}
	}

	private void closeConnection()
	{
		try
		{
			if(oos != null) oos.close();
		}
		catch(Exception _)
		{
			// Ignored
		}
		try
		{
			if(socket != null) socket.close();
		}
		catch(Exception _)
		{
			// Ignored
		}
		oos = null;
		socket = null;
	}

	@Override
	public void stop()
	{
		super.stop();
		scheduler.shutdownNow();
		closeConnection();
	}
}
