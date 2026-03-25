package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class BatchColorPadsRequest extends RequestMessage
{
	private Map<UUID, Color> padColors;
}
