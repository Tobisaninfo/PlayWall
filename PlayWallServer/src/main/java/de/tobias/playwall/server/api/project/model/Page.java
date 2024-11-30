package de.tobias.playwall.server.api.project.model;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Page
{
	private UUID id;
	private Integer position;
	private String name;
	private List<Pad> pads;
}
