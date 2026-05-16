package de.tobias.playwall.client.utils;

import java.util.Arrays;
import java.util.Objects;

public record ExportFile(String mimetype, byte[] data)
{
	@Override
	public boolean equals(Object object)
	{
		if(object == null || getClass() != object.getClass()) return false;
		ExportFile that = (ExportFile) object;
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
		return "ExportFile{" +
			   "mimetype='" + mimetype + '\'' +
			   ", data=" + Arrays.toString(data) +
			   '}';
	}
}
