package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class")
public abstract sealed class PadContent permits AudioPadContent
{
	public abstract PadContent copy();
}
