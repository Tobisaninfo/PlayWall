package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class PadLoadedUpdate extends UpdateMessage
{
	private UUID padId;
}
