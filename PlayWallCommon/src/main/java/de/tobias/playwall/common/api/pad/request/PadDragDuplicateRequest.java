package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadDragDuplicateRequest extends RequestMessage
{
	/**
	 * Source pad that gets duplicated onto the target pad location
	 */
	private UUID sourcePad;
	/**
	 * Pad get's replaced by a copy of the source pad
	 */
	private UUID targetPad;
}
