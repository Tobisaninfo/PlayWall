package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadChangeVolumeRequest extends RequestMessage
{
	private UUID padId;
	private double volume;
}
