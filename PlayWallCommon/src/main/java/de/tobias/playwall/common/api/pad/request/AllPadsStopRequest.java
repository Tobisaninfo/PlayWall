package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class AllPadsStopRequest extends RequestMessage
{
	private boolean isImmediately;
}
