package de.tobias.playwall.common.api.settings.audiodevices;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class AudioDevicesGetRequest extends RequestMessage
{
}
