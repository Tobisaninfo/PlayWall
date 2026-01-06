package de.tobias.playwall.client.model.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public final class FallbackPadContent extends PadContent
{
	@Override
	public FallbackPadContent copy()
	{
		return FallbackPadContent.builder().build();
	}
}
