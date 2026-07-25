package de.tobias.playwall.common.api.pad.update;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadSwapUpdate extends UpdateMessage
{
	private UUID pad1;
	private UUID pad2;
}
