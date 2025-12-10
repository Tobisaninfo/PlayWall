package de.tobias.playwall.client.launch;

import lombok.Getter;

import java.nio.file.Path;

public abstract class ServerLaunchException extends RuntimeException
{
	public ServerLaunchException(String message)
	{
		super(message);
	}

	public ServerLaunchException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public static class NotFoundException extends ServerLaunchException
	{
		@Getter
		private final Path path;

		public NotFoundException(Path path)
		{
			this.path = path;
			super(path + " not found");
		}
	}

	public static class PortInUseException extends ServerLaunchException
	{
		public PortInUseException()
		{
			super("Server port is in use");
		}
	}

	public static class GenericStartupException extends ServerLaunchException
	{
		public GenericStartupException()
		{
			super("Cannot startup server");
		}

		public GenericStartupException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}


}
