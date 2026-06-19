package de.tobias.playwall.common.api.page.request;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PageSettingsUpdateRequest extends RequestMessage
{
	private UUID pageId;
	private String name;
	private Color color;
}
