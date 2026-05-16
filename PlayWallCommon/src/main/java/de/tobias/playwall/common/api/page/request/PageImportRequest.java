package de.tobias.playwall.common.api.page.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class PageImportRequest extends RequestMessage
{
	private String mimetype;
	private String base64;
}
