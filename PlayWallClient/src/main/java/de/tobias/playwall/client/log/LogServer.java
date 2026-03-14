package de.tobias.playwall.client.log;

import de.tobias.playwall.common.api.LogEntry;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Listens on a TCP port and receives serialized LogEntry objects
 * from the custom RemoteLog4j2Appender running in client applications.
 */
public class LogServer
{
	public static final int DEFAULT_PORT = 4712;

	private final int port;
	private final Consumer<LogEntry> onEntry;
	private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "log-server");
		t.setDaemon(true);
		return t;
	});
	private volatile boolean running = false;

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
				System.out.println("[LogServer] Listening on port " + port);
				while(running)
				{
					Socket client = server.accept();
					executor.submit(() -> handleClient(client));
				}
			}
			catch(IOException e)
			{
				if(running) System.err.println("[LogServer] Error: " + e.getMessage());
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
	}

	private void handleClient(Socket socket)
	{
		String remote = socket.getRemoteSocketAddress().toString();
		System.out.println("[LogServer] Client connected: " + remote);
		try(ObjectInputStream ois = new ObjectInputStream(
				new BufferedInputStream(socket.getInputStream())))
		{
			while(true)
			{
				Object obj = ois.readObject();
				if(obj instanceof LogEntry)
				{
					onEntry.accept((LogEntry) obj);
				}
			}
		}
		catch(EOFException | java.net.SocketException e)
		{
			System.out.println("[LogServer] Client disconnected: " + remote);
		}
		catch(Exception e)
		{
			System.err.println("[LogServer] Client error: " + e.getMessage());
		}
	}

	public void stop()
	{
		running = false;
		executor.shutdownNow();
	}
}
