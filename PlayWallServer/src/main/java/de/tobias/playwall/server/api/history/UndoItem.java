package de.tobias.playwall.server.api.history;

import de.tobias.playwall.common.net.RequestMessage;

public record UndoItem(String shortDescription, String longDescription, RequestMessage request, RequestMessage inverseRequest)
{
}
