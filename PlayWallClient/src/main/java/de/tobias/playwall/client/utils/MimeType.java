package de.tobias.playwall.client.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MimeType
{
	APPLICATION_JSON("application/json", "json");

	private final String mimeTypeValue;
	private final String extension;

	public static MimeType getByMimeType(String mimeType)
	{
		for(MimeType type : values())
		{
			if(type.getMimeTypeValue().equals(mimeType))
			{
				return type;
			}
		}
		throw new IllegalArgumentException("No MimeType found for " + mimeType);
	}
}
