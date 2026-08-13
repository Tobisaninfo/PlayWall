package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class GlobalChangeVolumeRequest extends RequestMessage
{
	private double volume;
}
