package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadPlayPositionUpdate extends UpdateMessage
{
	public record PadPlayPosition(UUID padId, long millis)
	{
	}

	private List<PadPlayPosition> positions;
}
