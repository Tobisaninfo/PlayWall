package de.tobias.playwall.client.model.project;

import java.io.Serializable;

/**
 * Struktur um den Index eines Pads zu beschrieben.
 *
 * @author tobias
 * @since 6.0.0
 */
// Serializable is for Pad Drag and Drop necessary
public record PadIndex(int id, int page) implements Serializable
{
	public int getPagePosition()
	{
		return page;
	}

	@Override
	public String toString()
	{
		return String.valueOf(id);
	}

}
