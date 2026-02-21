package de.tobias.playwall.common.api.settings.update;

import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class SettingsUpdate extends UpdateMessage
{
	private SettingsDto settingsDto;
}
