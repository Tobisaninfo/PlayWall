package de.tobias.playwall.client.model.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
public abstract sealed class Pad permits AudioPad
{
	private UUID id;
	private Integer position;
	private String name;
	private List<String> mediaPaths;
}
