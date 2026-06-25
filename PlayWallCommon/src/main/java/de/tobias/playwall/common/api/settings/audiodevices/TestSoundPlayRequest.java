package de.tobias.playwall.common.api.settings.audiodevices;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class TestSoundPlayRequest extends RequestMessage
{
	private String audioDevice;
}
