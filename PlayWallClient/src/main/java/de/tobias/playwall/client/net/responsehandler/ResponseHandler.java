package de.tobias.playwall.client.net.responsehandler;

import de.tobias.playwall.common.net.RequestResponseMessage;

public interface ResponseHandler<T extends RequestResponseMessage>
{
	void handleResponse(T response);
}
