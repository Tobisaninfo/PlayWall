package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadSettingsUpdateRequest extends RequestMessage
{
	private UUID padId;
	private PadDto pad;
}
