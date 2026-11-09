package de.tobias.playwall.server.net.inspector;

import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.UpdateMessage;
import de.tobias.playwall.server.net.inspector.NetworkLogEntry.Direction;
import de.tobias.playwall.server.net.inspector.NetworkLogEntry.Kind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class NetworkLogServiceTest
{
	private static final String CLIENT_A = "127.0.0.1:50001";
	private static final String CLIENT_B = "127.0.0.1:50002";

	private final AtomicLong nanos = new AtomicLong();
	private NetworkLogService service;

	@BeforeEach
	void setUp()
	{
		service = new NetworkLogService(true, 100, nanos::get);
	}

	@Test
	void responseIsCorrelatedWithRequestAndHasDuration()
	{
		final RequestMessage request = new RequestMessage()
		{
		};
		service.logIncoming(CLIENT_A, request, "{\"req\":1}");
		nanos.addAndGet(42_000_000);
		service.logOutgoing(CLIENT_A, new ResponseMessage(request.getMessageId()), "{\"res\":1}");

		final List<NetworkLogEntry> entries = service.query(null, null, 0);
		assertEquals(2, entries.size());
		final NetworkLogEntry req = entries.get(0);
		final NetworkLogEntry res = entries.get(1);

		assertEquals(Kind.REQUEST, req.kind());
		assertEquals(Direction.IN, req.direction());
		assertEquals(Kind.RESPONSE, res.kind());
		assertEquals(Direction.OUT, res.direction());
		assertEquals(req.id(), res.correlatedId());
		assertEquals(42L, res.durationMs());
	}

	@Test
	void entriesCarryTheRegisteredClientType()
	{
		service.registerClientType(CLIENT_A, "COMPANION");
		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, "{}");
		service.logOutgoing(CLIENT_B, new UpdateMessage()
		{
		}, "{}");

		final List<NetworkLogEntry> entries = service.query(null, null, 0);
		assertEquals("COMPANION", entries.get(0).clientType());
		assertEquals(NetworkLogService.UNKNOWN_CLIENT_TYPE, entries.get(1).clientType());
	}

	@Test
	void errorIsCorrelatedWithRequest()
	{
		final RequestMessage request = new RequestMessage()
		{
		};
		service.logIncoming(CLIENT_A, request, "{}");
		service.logOutgoing(CLIENT_A, new ErrorMessage(request.getMessageId(), "boom", null), "{}");

		final NetworkLogEntry error = service.query(null, Kind.ERROR, 0).getFirst();
		assertNotNull(error.correlatedId());
		assertNotNull(error.durationMs());
	}

	@Test
	void sameMessageIdOnDifferentClientsIsNotMixedUp()
	{
		final RequestMessage request = new RequestMessage()
		{
		};
		service.logIncoming(CLIENT_A, request, "{}");
		service.logOutgoing(CLIENT_B, new ResponseMessage(request.getMessageId()), "{}");

		assertNull(service.query(CLIENT_B, Kind.RESPONSE, 0).getFirst().correlatedId());
	}

	@Test
	void updatesAreLoggedPerRecipientWithoutCorrelation()
	{
		final UpdateMessage update = new UpdateMessage()
		{
		};
		service.logOutgoing(CLIENT_A, update, "{}");
		service.logOutgoing(CLIENT_B, update, "{}");

		final List<NetworkLogEntry> updates = service.query(null, Kind.UPDATE, 0);
		assertEquals(2, updates.size());
		assertNull(updates.getFirst().correlatedId());
		assertNull(updates.getFirst().durationMs());
	}

	@Test
	void queryFiltersByClientAndAfterId()
	{
		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, "{}");
		service.logOutgoing(CLIENT_B, new UpdateMessage()
		{
		}, "{}");
		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, "{}");

		assertEquals(2, service.query(CLIENT_A, null, 0).size());
		assertEquals(1, service.query(CLIENT_A, null, 1).size());
		assertEquals(List.of(CLIENT_A, CLIENT_B), service.getClients());
	}

	@Test
	void ringBufferDropsOldestEntries()
	{
		service = new NetworkLogService(true, 3, nanos::get);
		for(int i = 0; i < 5; i++)
		{
			service.logOutgoing(CLIENT_A, new UpdateMessage()
			{
			}, "{}");
		}

		final List<NetworkLogEntry> entries = service.query(null, null, 0);
		assertEquals(3, entries.size());
		assertEquals(3L, entries.getFirst().id());
	}

	@Test
	void largePayloadIsTruncatedButKeepsOriginalSize()
	{
		final String payload = "x".repeat(NetworkLogService.MAX_PAYLOAD_CHARS + 10);
		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, payload);

		final NetworkLogEntry entry = service.query(null, null, 0).getFirst();
		assertTrue(entry.truncated());
		assertEquals(NetworkLogService.MAX_PAYLOAD_CHARS, entry.payload().length());
		assertEquals(payload.length(), entry.sizeBytes());
	}

	@Test
	void listenersReceiveNewEntriesAndFailuresAreIgnored()
	{
		final List<NetworkLogEntry> received = new ArrayList<>();
		service.addListener(e -> {
			throw new IllegalStateException("broken listener");
		});
		service.addListener(received::add);

		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, "{}");
		assertEquals(1, received.size());
	}

	@Test
	void disabledServiceLogsNothing()
	{
		service = new NetworkLogService(false, 100, nanos::get);
		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, "{}");
		service.logIncoming(CLIENT_A, new RequestMessage()
		{
		}, "{}");

		assertTrue(service.query(null, null, 0).isEmpty());
	}

	@Test
	void clearRemovesEntriesAndClients()
	{
		service.logOutgoing(CLIENT_A, new UpdateMessage()
		{
		}, "{}");
		service.clear();

		assertTrue(service.query(null, null, 0).isEmpty());
		assertTrue(service.getClients().isEmpty());
	}
}
