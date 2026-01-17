package de.tobias.playwall.common.api.pad.update;

import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadUpdate extends UpdateMessage
{
	private PadDto pad;
}
