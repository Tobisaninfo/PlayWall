package de.tobias.playwall.server.history;

import de.tobias.playwall.common.net.RequestMessage;

public record UndoItem(String description, RequestMessage request, RequestMessage inverseRequest)
{
}
