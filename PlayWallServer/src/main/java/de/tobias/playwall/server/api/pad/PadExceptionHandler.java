package de.tobias.playwall.server.api.pad;

import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.net.exception.WsExceptionAdvice;
import de.tobias.playwall.server.net.exception.WsExceptionHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@WsExceptionAdvice
@AllArgsConstructor
public class PadExceptionHandler
{
	private final MessageSource messageSource;

	@WsExceptionHandler(PadNotExistsException.class)
	ErrorMessage handlePadNotExistsException(RequestMessage requestMessage, PadNotExistsException e)
	{
		final PadNotExistsError error = new PadNotExistsError(e.getProjectId(), e.getPadId());
		return new ErrorMessage(requestMessage.getMessageId(), messageSource.getMessage(error.getLocalizationKey(), error.getMessageArguments(), LocaleContextHolder.getLocale()), error);
	}
}
