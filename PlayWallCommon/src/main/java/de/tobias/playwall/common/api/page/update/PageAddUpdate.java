package de.tobias.playwall.common.api.page.update;

import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PageAddUpdate extends UpdateMessage
{
	private PageDto page;
}
