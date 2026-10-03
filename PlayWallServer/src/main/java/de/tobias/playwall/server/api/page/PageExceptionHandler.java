package de.tobias.playwall.server.api.page;

import de.tobias.playwall.common.api.page.PageNotExistsError;
import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.net.exception.WsExceptionAdvice;
import de.tobias.playwall.server.net.exception.WsExceptionHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@WsExceptionAdvice
@AllArgsConstructor
public class PageExceptionHandler
{
	private final MessageSource messageSource;

	@WsExceptionHandler(PageNotExistsException.class)
	ErrorMessage handlePageNotExistsException(RequestMessage requestMessage, PageNotExistsException e)
	{
		final PageNotExistsError error = new PageNotExistsError(e.getPageId());
		return new ErrorMessage(requestMessage.getMessageId(), messageSource.getMessage(error.getLocalizationKey(), error.getMessageArguments(), LocaleContextHolder.getLocale()), error);
	}
}
