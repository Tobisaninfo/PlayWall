package de.tobias.playwall.common.api.settings;

import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class SettingsUpdateRequest extends RequestMessage
{
	private SettingsDto settings;
}
