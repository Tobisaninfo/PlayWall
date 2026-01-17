package de.tobias.playwall.client.domain.pad;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
public abstract sealed class PadContent permits AudioPadContent
{
	public abstract PadContent copy();
}
