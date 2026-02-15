package de.tobias.playwall.common.api;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class CompoundRequest extends RequestMessage
{

	public CompoundRequest(RequestMessage... requests)
	{
		this.requests = List.of(requests);
	}

	private List<RequestMessage> requests;
}
