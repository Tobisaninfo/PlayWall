package de.tobias.playwall.common.api.settings;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class SettingsGetRequest extends RequestMessage
{
}
