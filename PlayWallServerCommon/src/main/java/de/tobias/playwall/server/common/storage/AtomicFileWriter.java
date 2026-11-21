package de.tobias.playwall.server.common.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes files so that a crash or power loss never leaves a truncated or zero-filled file behind:
 * the content is written to a temporary file in the same directory, flushed to disk and then moved over the target.
 */
@Slf4j
public final class AtomicFileWriter
{
	@FunctionalInterface
	public interface OutputStreamWriter
	{
		void write(OutputStream outputStream) throws IOException;
	}

	private AtomicFileWriter()
	{
	}

	public static void write(Path target, OutputStreamWriter writer) throws IOException
	{
		final Path directory = target.toAbsolutePath().getParent();
		Files.createDirectories(directory);

		final Path tempFile = Files.createTempFile(directory, target.getFileName().toString(), ".tmp");
		try
		{
			try(FileOutputStream outputStream = new FileOutputStream(tempFile.toFile()))
			{
				writer.write(outputStream);
				outputStream.flush();
				sync(outputStream);
			}
			move(tempFile, target);
		}
		finally
		{
			Files.deleteIfExists(tempFile);
		}
	}

	private static void sync(FileOutputStream outputStream)
	{
		try
		{
			outputStream.getFD().sync();
		}
		catch(IOException e)
		{
			if(!(e instanceof SyncFailedException))
			{
				// Not every file system supports fsync; the atomic move still protects against partially written files.
				log.debug("Cannot sync file to disk", e);
			}
		}
	}

	private static void move(Path source, Path target) throws IOException
	{
		try
		{
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(AtomicMoveNotSupportedException e)
		{
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
