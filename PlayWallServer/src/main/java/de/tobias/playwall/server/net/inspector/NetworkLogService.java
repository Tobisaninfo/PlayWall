package de.tobias.playwall.server.net.inspector;

import de.tobias.playwall.common.net.*;
import de.tobias.playwall.server.net.inspector.NetworkLogEntry.Direction;
import de.tobias.playwall.server.net.inspector.NetworkLogEntry.Kind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * In-memory log of all messages exchanged with the clients, used by the network viewer.
 */
@Slf4j
@Service
public class NetworkLogService
{
	static final int MAX_PAYLOAD_CHARS = 256 * 1024;
	private static final long MAX_STORED_BYTES = 64L * 1024 * 1024;
	private static final int MAX_PENDING_REQUESTS = 1000;
	static final String UNKNOWN_CLIENT_TYPE = "UNKNOWN";
	static final String CLIENT_TYPE_HEADER = "X-Client-Type";
	private static final String UNKNOWN_CLIENT = "unknown";

	private record PendingRequest(long entryId, long startNanos)
	{
	}

	private final boolean enabled;
	private final int maxEntries;
	private final LongSupplier nanoClock;

	private final Deque<NetworkLogEntry> entries = new ArrayDeque<>();
	private final Set<String> clients = new LinkedHashSet<>();
	private final Map<String, String> clientTypes = new HashMap<>();
	private final Map<String, PendingRequest> pendingRequests = new LinkedHashMap<>()
	{
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, PendingRequest> eldest)
		{
			return size() > MAX_PENDING_REQUESTS;
		}
	};
	private final List<Consumer<NetworkLogEntry>> listeners = new CopyOnWriteArrayList<>();

	private long nextId = 1;
	private long storedBytes = 0;

	@Autowired
	public NetworkLogService(@Value("${de.tobias.playwall.network-inspector.enabled:true}") boolean enabled,
							 @Value("${de.tobias.playwall.network-inspector.max-entries:5000}") int maxEntries)
	{
		this(enabled, maxEntries, System::nanoTime);
	}

	NetworkLogService(boolean enabled, int maxEntries, LongSupplier nanoClock)
	{
		this.enabled = enabled;
		this.maxEntries = maxEntries;
		this.nanoClock = nanoClock;
	}

	public static String clientOf(WebSocketSession session)
	{
		final InetSocketAddress address = session.getRemoteAddress();
		if(address == null || address.getAddress() == null)
		{
			return UNKNOWN_CLIENT;
		}
		final String host = address.getAddress().getHostAddress();
		return (host.contains(":") ? "[" + host + "]" : host) + ":" + address.getPort();
	}

	public static String clientTypeOf(WebSocketSession session)
	{
		final String type = session.getHandshakeHeaders().getFirst(CLIENT_TYPE_HEADER);
		return type == null || type.isBlank() ? UNKNOWN_CLIENT_TYPE : type.trim().toUpperCase(Locale.ROOT);
	}

	public synchronized void registerClientType(String client, String clientType)
	{
		if(enabled)
		{
			clientTypes.put(client, clientType);
		}
	}

	public void logIncoming(String client, RequestMessage message, String payload)
	{
		if(!enabled)
		{
			return;
		}
		final NetworkLogEntry entry;
		synchronized(this)
		{
			entry = store(Direction.IN, Kind.REQUEST, message, client, payload, null, null);
			pendingRequests.put(pendingKey(client, message.getMessageId()), new PendingRequest(entry.id(), nanoClock.getAsLong()));
		}
		notifyListeners(entry);
	}

	public void logOutgoing(String client, BaseMessage message, String payload)
	{
		if(!enabled)
		{
			return;
		}
		final Kind kind = switch(message)
		{
			case ErrorMessage _ -> Kind.ERROR;
			case ResponseMessage _ -> Kind.RESPONSE;
			case UpdateMessage _ -> Kind.UPDATE;
			case RequestMessage _ -> Kind.REQUEST;
		};

		final NetworkLogEntry entry;
		synchronized(this)
		{
			PendingRequest pending = null;
			if(kind == Kind.RESPONSE || kind == Kind.ERROR)
			{
				pending = pendingRequests.remove(pendingKey(client, message.getMessageId()));
			}
			final Long correlatedId = pending != null ? pending.entryId() : null;
			final Long durationMs = pending != null ? (nanoClock.getAsLong() - pending.startNanos()) / 1_000_000 : null;
			entry = store(Direction.OUT, kind, message, client, payload, correlatedId, durationMs);
		}
		notifyListeners(entry);
	}

	/**
	 * @param client  only entries of this client, or all if {@code null}
	 * @param kind    only entries of this kind, or all if {@code null}
	 * @param afterId only entries with an id greater than this
	 */
	public synchronized List<NetworkLogEntry> query(String client, Kind kind, long afterId)
	{
		return entries.stream()
				.filter(e -> e.id() > afterId)
				.filter(e -> client == null || client.isBlank() || client.equals(e.clientAddress()))
				.filter(e -> kind == null || kind == e.kind())
				.toList();
	}

	public synchronized List<String> getClients()
	{
		return List.copyOf(clients);
	}

	public synchronized void clear()
	{
		entries.clear();
		clients.clear();
		clientTypes.clear();
		pendingRequests.clear();
		storedBytes = 0;
	}

	public void addListener(Consumer<NetworkLogEntry> listener)
	{
		listeners.add(listener);
	}

	private NetworkLogEntry store(Direction direction, Kind kind, BaseMessage message, String client, String payload, Long correlatedId, Long durationMs)
	{
		final int sizeBytes = payload.getBytes(StandardCharsets.UTF_8).length;
		final boolean truncated = payload.length() > MAX_PAYLOAD_CHARS;
		final String storedPayload = truncated ? payload.substring(0, MAX_PAYLOAD_CHARS) : payload;

		final NetworkLogEntry entry = new NetworkLogEntry(nextId++, System.currentTimeMillis(), direction, kind,
				message.getClass().getSimpleName(), message.getMessageId(), client, clientTypes.getOrDefault(client, UNKNOWN_CLIENT_TYPE), storedPayload, sizeBytes, truncated, correlatedId, durationMs);

		entries.addLast(entry);
		clients.add(client);
		storedBytes += storedPayload.length();

		while(entries.size() > maxEntries || storedBytes > MAX_STORED_BYTES)
		{
			final NetworkLogEntry removed = entries.pollFirst();
			if(removed == null)
			{
				break;
			}
			storedBytes -= removed.payload().length();
		}
		return entry;
	}

	private void notifyListeners(NetworkLogEntry entry)
	{
		for(Consumer<NetworkLogEntry> listener : listeners)
		{
			try
			{
				listener.accept(entry);
			}
			catch(Exception e)
			{
				log.warn("Network log listener failed", e);
			}
		}
	}

	private static String pendingKey(String client, UUID messageId)
	{
		return client + "/" + messageId;
	}
}
