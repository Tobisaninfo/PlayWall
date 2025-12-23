package de.tobias.playwall.common.utils;

import de.thecodelabs.utils.io.PathUtils;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class FileFormats
{
	public enum PadContentType
	{
		AUDIO
	}

	public record FileFormat(PadContentType contentType, List<String> extensions)
	{
	}

	public static final List<FileFormat> FILE_FORMATS = List.of(new FileFormat(PadContentType.AUDIO, List.of("mp3", "wav", "flac")));

	public static PadContentType getContentTypeForFile(Path path)
	{
		final String extension = PathUtils.getFileExtension(path).toLowerCase();
		return FILE_FORMATS.stream().filter(format -> format.extensions().contains(extension)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported file extension " + extension)).contentType();
	}
}
