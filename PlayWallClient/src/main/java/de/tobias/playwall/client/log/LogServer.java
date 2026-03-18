package de.tobias.playwall.client.log;

import de.tobias.playwall.common.api.LogEntry;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Listens on a TCP port and receives serialized LogEntry objects
 * from the custom RemoteLog4j2Appender running in client applications.
 */
@Slf4j
public class LogServer
{
	public static final int DEFAULT_PORT = 4712;

	private final int port;
	private final Consumer<LogEntry> onEntry;
	private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
		final Thread t = new Thread(r, "log-server");
		t.setDaemon(true);
		return t;
	});
	private boolean running = false;

	private final IntegerProperty activeConnections = new SimpleIntegerProperty();

	public ReadOnlyIntegerProperty activeConnectionsProperty()
	{
		return activeConnections;
	}

	public LogServer(int port, Consumer<LogEntry> onEntry)
	{
		this.port = port;
		this.onEntry = onEntry;
	}

	public void start()
	{
		running = true;
		executor.submit(() -> {
			try(ServerSocket server = new ServerSocket(port))
			{
				server.setReuseAddress(true);
				log.info("Listening on port {}", port);
				while(running)
				{
					final Socket client = server.accept();
					activeConnections.set(activeConnections.get() + 1);
					executor.submit(() -> handleClient(client));
				}
			}
			catch(IOException e)
			{
				if(running) log.error("Error on LogServer", e);
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
	}

	private void handleClient(Socket socket)
	{
		final String remote = socket.getRemoteSocketAddress().toString();
		log.info("Client connected: {}", remote);

		try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream())))
		{
			while(running)
			{
				Object obj = ois.readObject();
				if(obj instanceof LogEntry entry)
				{
					onEntry.accept(entry);
				}
			}
		}
		catch(EOFException | SocketException _)
		{
			log.info("Client disconnected: {}", remote);
		}
		catch(Exception e)
		{
			log.error("Client error", e);
		}
		activeConnections.set(activeConnections.get() - 1);
	}

	public void stop()
	{
		running = false;
		executor.shutdownNow();
	}
}
