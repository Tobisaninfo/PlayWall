package de.tobias.playwall.server.api.project;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Page
{
	private UUID id;
	private Integer position;
	private String name;
	private List<Pad> pads;
}
