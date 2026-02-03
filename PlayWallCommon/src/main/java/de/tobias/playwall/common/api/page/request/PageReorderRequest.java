package de.tobias.playwall.common.api.page.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@ToString(callSuper = true)
public class PageReorderRequest extends RequestMessage
{
	private Map<UUID, Integer> positions;
}
