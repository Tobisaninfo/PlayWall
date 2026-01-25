package de.tobias.playwall.server.net;

public sealed interface RequestHandler permits OneTimeActionRequestHandler, GetRequestHandler, UndoableRequestHandler
{
}
