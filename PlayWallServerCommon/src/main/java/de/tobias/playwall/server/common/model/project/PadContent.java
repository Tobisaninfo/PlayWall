package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@name")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AudioPadContent.class, name = "AudioPadContent")
})
public abstract sealed class PadContent permits AudioPadContent
{
	public abstract PadContent copy();
}
