package de.tobias.playwall.server.common.migration;

import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

final class JsonPathOperations
{
	private JsonPathOperations()
	{
	}

	static JsonNode remove(JsonNode root, String path)
	{
		final JsonPointer pointer = JsonPointer.compile(path);
		if(pointer.matches())
		{
			throw new MigrationException("Cannot delete '" + path + "': the root node cannot be deleted");
		}

		final JsonNode parent = parent(root, pointer);
		if(parent.isObject())
		{
			final ObjectNode object = (ObjectNode) parent;
			final String property = pointer.last().getMatchingProperty();
			if(!object.has(property))
			{
				throw new MigrationException("Cannot delete '" + path + "': path does not exist");
			}
			return object.remove(property);
		}

		if(parent.isArray())
		{
			final ArrayNode array = (ArrayNode) parent;
			final int index = pointer.last().getMatchingIndex();
			if(!pointer.last().mayMatchElement() || index < 0 || index >= array.size())
			{
				throw new MigrationException("Cannot delete '" + path + "': path does not exist");
			}
			return array.remove(index);
		}

		throw new MigrationException("Cannot delete '" + path + "': path does not exist");
	}

	static void add(JsonNode root, String path, JsonNode value)
	{
		final JsonPointer pointer = JsonPointer.compile(path);
		if(pointer.matches())
		{
			throw new MigrationException("Cannot add at '" + path + "': the root node cannot be replaced");
		}
		((ObjectNode) root).put(pointer, value);
	}

	private static JsonNode parent(JsonNode root, JsonPointer pointer)
	{
		final JsonPointer parentPath = pointer.head();
		return parentPath.matches() ? root : root.at(parentPath);
	}
}