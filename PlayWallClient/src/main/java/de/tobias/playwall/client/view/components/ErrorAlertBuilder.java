package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.ServerError;
import de.tobias.playwall.common.api.StackTraceError;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Window;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(onConstructor_ = {@InjectConstructor})
public class ErrorAlertBuilder
{
	private final App app;

	public Alert createErrorAlert(String title, String headerText, String contentText, ServerError error, Window owner)
	{
		String stackStrace = null;
		if(error instanceof StackTraceError stackTraceError)
		{
			stackStrace = stackTraceError.getStackTrace();
		}
		return createErrorAlert(title, headerText, contentText, stackStrace, owner);
	}

	public Alert createErrorAlert(String title, String headerText, String contentText, Window owner)
	{
		return createErrorAlert(title, headerText, contentText, (String) null, owner);
	}

	public Alert createErrorAlert(String title, String headerText, String contentText, String stackTrace, Window owner)
	{
		final Alert alert = Alerts.getInstance().createAlert(Alert.AlertType.ERROR, title, headerText, contentText, owner);
		if(stackTrace != null)
		{
			final TextArea content = new TextArea(stackTrace);
			content.setEditable(false);
			alert.getDialogPane().setExpandableContent(content);

			final ButtonType logsButtonType = new ButtonType(Localization.getString("ui.button.show_log"), ButtonBar.ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().add(logsButtonType);
			Button okButton = (Button) alert.getDialogPane().lookupButton(logsButtonType);

			okButton.addEventFilter(ActionEvent.ACTION, _ -> NativeApplication.sharedInstance().showFileInFileViewer(app.getPath(PathType.LOG)));
		}

		return alert;
	}
}
