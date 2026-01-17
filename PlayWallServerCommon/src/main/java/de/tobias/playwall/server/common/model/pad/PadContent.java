package de.tobias.playwall.server.common.model.pad;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@name")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AudioPadContent.class, name = "AudioPadContent")
})
@EqualsAndHashCode
public abstract sealed class PadContent permits AudioPadContent
{
	public abstract PadContent copy();
}
