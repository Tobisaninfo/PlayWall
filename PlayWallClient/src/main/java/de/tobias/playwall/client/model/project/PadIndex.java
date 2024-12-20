package de.tobias.playwall.client.model.project;

import lombok.Getter;

import java.io.Serializable;

/**
 * Struktur um den Index eines Pads zu beschrieben.
 *
 * @author tobias
 * @since 6.0.0
 */
// Serializable is for Pad Drag and Drop necessary
public class PadIndex implements Serializable {

	@Getter
	private final int id;
	private final int page;

	public PadIndex(int id, int page) {
		this.id = id;
		this.page = page;
	}

	public int getPagePosition() {
		return page;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + page;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PadIndex other = (PadIndex) obj;
		if (id != other.id)
			return false;
		return page == other.page;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

}
