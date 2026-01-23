package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
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

	private static class Listener2
	{

		@EventListener(TestUpdateMessage.class)
		void test(TestUpdateMessage message)
		{
			// Nothing to do
		}

	}

	private UpdateMessageEventHandler handler;

	@BeforeEach
	void init()
	{
		handler = new UpdateMessageEventHandler();
	}

	// Interface based

	@Test
	void testRegisterListenerInterfaceAndFireEvent()
	{
		final Listener1 listener = Mockito.spy(Listener1.class);
		handler.registerListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener).onUpdateMessage(any(TestUpdateMessage.class));
	}

	@Test
	void testRegisterListenerInterfaceAndFireDifferentEvent()
	{
		final Listener1 listener = Mockito.spy(Listener1.class);
		handler.registerListener(listener);

		handler.fireEvent(new OtherUpdateMessage());
		verify(listener, never()).onUpdateMessage(any(TestUpdateMessage.class));
	}

	@Test
	void testUnregisterListenerInterfaceAndFireEvent()
	{
		final Listener1 listener = Mockito.spy(Listener1.class);
		handler.registerListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener).onUpdateMessage(any(TestUpdateMessage.class));
		Mockito.reset(listener);

		handler.unregisterListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener, never()).onUpdateMessage(any(TestUpdateMessage.class));
	}

	// Annotation based

	@Test
	void testRegisterListenerAnnotationAndFireEvent()
	{
		final Listener2 listener = Mockito.spy(Listener2.class);
		handler.registerListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener).test(any(TestUpdateMessage.class));
	}

	@Test
	void testRegisterListenerAnnotationAndFireDifferentEvent()
	{
		final Listener2 listener = Mockito.spy(Listener2.class);
		handler.registerListener(listener);

		handler.fireEvent(new OtherUpdateMessage());
		verify(listener, never()).test(any(TestUpdateMessage.class));
	}

	@Test
	void testUnregisterAnnotationListenerAndFireEvent()
	{
		final Listener2 listener = Mockito.spy(Listener2.class);
		handler.registerListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener).test(any(TestUpdateMessage.class));
		Mockito.reset(listener);

		handler.unregisterListener(listener);

		handler.fireEvent(new TestUpdateMessage());
		verify(listener, never()).test(any(TestUpdateMessage.class));
	}
}
