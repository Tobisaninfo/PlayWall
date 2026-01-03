package de.tobias.playwall.client.model.project;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadContent content;

	private PadStatus status;

	public String getReadablePosition()
	{
		return String.valueOf(position + 1);
	}
}
