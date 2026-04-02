package de.tobias.playwall.client.domain.project;

import java.util.Arrays;
import java.util.Objects;

public record ProjectExport(String mimetype, byte[] data)
{
	@Override
	public boolean equals(Object object)
	{
		if(object == null || getClass() != object.getClass()) return false;
		ProjectExport that = (ProjectExport) object;
		return Objects.deepEquals(data, that.data) && Objects.equals(mimetype, that.mimetype);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(mimetype, Arrays.hashCode(data));
	}

	@Override
	public String toString()
	{
		return "ProjectExport{" +
			   "mimetype='" + mimetype + '\'' +
			   ", data=" + Arrays.toString(data) +
			   '}';
	}
}
