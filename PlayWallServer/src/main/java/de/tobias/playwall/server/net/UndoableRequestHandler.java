package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.history.UndoItem;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

/**
 * Perform an action on the server that can be undone.
 */
@AllArgsConstructor
public abstract non-sealed class UndoableRequestHandler<T extends RequestMessage> implements RequestHandler
{
	protected final MessageSource messageSource;
	protected final ApplicationContext context;

	public abstract Optional<UndoItem> handleRequest(T requestMessage) throws IOException;
}
