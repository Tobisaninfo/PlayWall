package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UpdateMessageEventHandlerTest
{
	private static class TestUpdateMessage extends UpdateMessage
	{
	}

	private static class OtherUpdateMessage extends UpdateMessage
	{
	}

	private static class Listener1 implements UpdateMessageEventListener<TestUpdateMessage>
	{

		@Override
		public void onUpdateMessage(TestUpdateMessage message)
		{
			// Nothing to do
		}

		@Override
		public Class<TestUpdateMessage> getMessageClass()
		{
			return TestUpdateMessage.class;
		}
	}

	private UpdateMessageEventHandler handler;

	@BeforeEach
	void init()
	{
		handler = new UpdateMessageEventHandler();
	}

	@Test
	void testRegisterListenerAndFireEvent()
	{
		final Listener1 listener = Mockito.spy(Listener1.class);
		handler.registerListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener).onUpdateMessage(Mockito.any(TestUpdateMessage.class));
	}

	@Test
	void testRegisterListenerAndFireDifferentEvent()
	{
		final Listener1 listener = Mockito.spy(Listener1.class);
		handler.registerListener(listener);

		handler.fireEvent(new OtherUpdateMessage());
		verify(listener, never()).onUpdateMessage(Mockito.any(TestUpdateMessage.class));
	}

	@Test
	void testUnregisterListenerAndFireEvent()
	{
		final Listener1 listener = Mockito.spy(Listener1.class);
		handler.registerListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener).onUpdateMessage(Mockito.any(TestUpdateMessage.class));
		Mockito.reset(listener);

		handler.unregisterListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener, never()).onUpdateMessage(Mockito.any(TestUpdateMessage.class));
	}
}
