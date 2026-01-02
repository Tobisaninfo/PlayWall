package de.tobias.playwall.client.model.project;

import java.io.Serializable;

/**
 * Struktur um den Index eines Pads zu beschrieben.
 *
 * @author tobias
 * @since 6.0.0
 */
// Serializable is for Pad Drag and Drop necessary
public record PadIndex(int padPosition, int pagePosition) implements Serializable
{
	public int getPagePosition()
	{
		return pagePosition;
	}

	@Override
	public String toString()
	{
		return String.valueOf(padPosition);
	}

}
