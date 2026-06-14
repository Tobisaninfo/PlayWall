package de.tobias.playwall.common.api.pad.update;

import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadReplaceUpdate extends UpdateMessage
{
	/**
	 * New pad to replace the old one (target)
	 */
	private PadDto sourcePad;
	/**
	 * The target there the new pad (source) should be placed
	 */
	private UUID targetPadId;
}
