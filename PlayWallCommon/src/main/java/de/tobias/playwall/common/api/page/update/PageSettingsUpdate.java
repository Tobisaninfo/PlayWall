package de.tobias.playwall.common.api.page.update;

import de.tobias.playwall.common.api.page.PageSettingsDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PageSettingsUpdate extends UpdateMessage
{
	private UUID pageId;
	private PageSettingsDto pageSettings;
}
