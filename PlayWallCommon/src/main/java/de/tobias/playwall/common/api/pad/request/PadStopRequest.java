package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadStopRequest extends RequestMessage implements PadIdRequest
{
	private UUID padId;
}
