package de.tobias.playwall.common.api.pad.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class BatchReplaceMediaRequest extends RequestMessage
{
	private Map<UUID, String> newMediaPathsByPadId;
	private Set<UUID> padIdsToDelete;
}
