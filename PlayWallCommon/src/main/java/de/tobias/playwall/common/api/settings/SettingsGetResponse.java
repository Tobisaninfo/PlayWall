package de.tobias.playwall.common.api.settings;

import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class SettingsGetResponse extends ResponseMessage
{
	private SettingsDto settings;

	public SettingsGetResponse(UUID messageId, SettingsDto settings)
	{
		super(messageId);
		this.settings = settings;
	}
}
