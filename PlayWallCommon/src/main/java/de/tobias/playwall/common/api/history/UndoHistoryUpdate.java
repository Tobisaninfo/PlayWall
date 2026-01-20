package de.tobias.playwall.common.api.history;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class UndoHistoryUpdate extends UpdateMessage
{
	private String nextUndoOperation;
	private String nextRedoOperation;
	private String message;
}
