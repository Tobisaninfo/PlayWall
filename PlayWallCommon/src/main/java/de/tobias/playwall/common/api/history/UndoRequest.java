package de.tobias.playwall.common.api.history;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString(callSuper = true)
public class UndoRequest extends RequestMessage
{
}
