package de.tobias.playwall.common.api.pad.update;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PadWarningAnimationPlayUpdate extends UpdateMessage
{
	private UUID padId;
	private boolean isPlaying;
}
