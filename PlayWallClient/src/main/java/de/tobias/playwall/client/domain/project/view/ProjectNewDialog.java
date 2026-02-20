package de.tobias.playwall.client.domain.project.view;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.ModalDialogBase;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.settings.SettingsPageWithButtons;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

@ViewController(path = "de/tobias/playwall/client/view/dialog", view = "ProjectNewDialog")
@Getter(AccessLevel.PACKAGE)
public class ProjectNewDialog extends ModalDialogBase<ProjectMetadata>
{
	@FXML
	private SettingsPageWithButtons settingsPage;
	@FXML
	private TextField textFieldName;
	@FXML
	private Spinner<Integer> spinnerNumberOfHorizontalPads;
	@FXML
	private Spinner<Integer> spinnerNumberOfVerticalPads;

	@Getter(AccessLevel.NONE)
	private final FluentClient client;

	@Getter(AccessLevel.NONE)
	private ProjectMetadata project;

	private final ErrorAlertBuilder errorAlertBuilder;

	@InjectConstructor
	ProjectNewDialog(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		this.client = client;
		this.errorAlertBuilder = errorAlertBuilder;
	}

	@Override
	protected void init()
	{
		settingsPage.getSaveButton().disableProperty().bind(textFieldName.textProperty().isEmpty());
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		stage.setTitle(Localization.getString(Strings.UI_DIALOG_NEW_PROJECT_TITLE));
		stage.setWidth(560);
		stage.setHeight(380);

		stage.setMinWidth(560);
		stage.setMinHeight(380);

		stage.setMaxWidth(560);
		stage.setMaxHeight(380);
	}

	@Override
	protected ProjectMetadata getResultValue()
	{
		return project;
	}

	@FXML
	private void finishButtonHandler(ActionEvent event)
	{
		final String name = textFieldName.getText();
		final int numberOfHorizontalPads = spinnerNumberOfHorizontalPads.getValue();
		final int numberOfVerticalPads = spinnerNumberOfVerticalPads.getValue();

		try
		{
			project = client.projects().add(name, numberOfHorizontalPads, numberOfVerticalPads);
			Platform.runLater(() -> getStageContainer().ifPresent(NVCStage::close));
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_ADD), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event)
	{
		getStageContainer().ifPresent(NVCStage::close);
	}
}
