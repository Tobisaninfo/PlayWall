package de.tobias.playwall.common.api.page.update;

import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PageDeleteUpdate extends UpdateMessage
{
	private UUID pageId;
	private Map<UUID, Integer> positions;
}
