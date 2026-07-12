package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadSetRequest extends RequestMessage
{
	private UUID targetPadId;

	/**
	 * Source pad that gets duplicated onto the target pad location
	 */
	private PadDto pad;
}
