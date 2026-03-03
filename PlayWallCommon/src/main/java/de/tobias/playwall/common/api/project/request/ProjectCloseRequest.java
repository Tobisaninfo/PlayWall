package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class ProjectCloseRequest extends RequestMessage
{
}
