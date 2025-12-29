package de.tobias.playwall.server.api;

import de.tobias.playwall.common.api.ServerError;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@Getter
public class PlayWallServerException extends Exception
{
	private final transient ServerError error;

	public PlayWallServerException(MessageSource messageSource, ServerError error)
	{
		super(messageSource.getMessage(error.getLocalizationKey(), error.getMessageArguments(), LocaleContextHolder.getLocale()));
		this.error = error;
	}

	public PlayWallServerException(String message, ServerError error)
	{
		super(message);
		this.error = error;
	}
}
