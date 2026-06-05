package de.tobias.playwall.client.server;

import lombok.Getter;

import java.nio.file.Path;

public abstract class ServerLaunchException extends RuntimeException
{
	protected ServerLaunchException(String message)
	{
		super(message);
	}

	protected ServerLaunchException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public static class NotFoundException extends ServerLaunchException
	{
		@Getter
		private final transient Path path;

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

	public static class TimeoutException extends ServerLaunchException
	{
		public TimeoutException(Throwable cause)
		{
			super("Timeout for server startup reached", cause);
		}
	}

	public static class PermissionException extends ServerLaunchException
	{
		public PermissionException()
		{
			super("Execute permission cannot be set for server binary");
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
