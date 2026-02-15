package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.StringUtils;
import de.tobias.playwall.common.api.ServerError;
import de.tobias.playwall.common.api.StackTraceError;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Window;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ErrorAlert
{
	public static Alert createErrorAlert(String title, String headerText, String contentText, ServerError error, Window owner)
	{
		String stackStrace = null;
		if(error instanceof StackTraceError stackTraceError)
		{
			stackStrace = stackTraceError.getStackTrace();
		}
		return createErrorAlert(title, headerText, contentText, stackStrace, owner);
	}

	public static Alert createErrorAlert(String title, String headerText, String contentText, String stackTrace, Window owner)
	{
		final Alert alert = Alerts.getInstance().createAlert(Alert.AlertType.ERROR, title, headerText, contentText, owner);
		if(stackTrace != null)
		{
			final TextArea content = new TextArea(stackTrace);
			content.setEditable(false);
			alert.getDialogPane().setExpandableContent(content);
		}
		return alert;
	}
}
