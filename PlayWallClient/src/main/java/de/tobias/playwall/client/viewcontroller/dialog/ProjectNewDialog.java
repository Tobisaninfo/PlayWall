package de.tobias.playwall.client.viewcontroller.dialog;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.style.Styleable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

public class ProjectNewDialog extends NVC
{
	private static final int MIN_NUMBER_OF_PADS_PER_AXIS = 3;
	private static final int MAX_NUMBER_OF_PADS_PER_AXIS = 10;

	@FXML
	private TextField nameTextField;
	@FXML
	private Spinner<Integer> spinnerNumberOfHorizontalPads;
	@FXML
	private Spinner<Integer> spinnerNumberOfVerticalPads;
	@FXML
	private Button finishButton;
	@FXML
	private Button cancelButton;

	private final Client client;

	private ProjectMetadata project;

	public ProjectNewDialog(Window owner, Client client)
	{
		this.client = client;
		load("de/tobias/playwall/client/view/dialog", "NewProjectDialog", Localization.getBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	@Override
	public void init()
	{
		nameTextField.textProperty().addListener((a, b, c) -> finishButton.setDisable(c.trim().isEmpty()));
		finishButton.setDisable(true);

		spinnerNumberOfHorizontalPads.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_NUMBER_OF_PADS_PER_AXIS, MAX_NUMBER_OF_PADS_PER_AXIS, 6));
		spinnerNumberOfVerticalPads.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_NUMBER_OF_PADS_PER_AXIS, MAX_NUMBER_OF_PADS_PER_AXIS, 4));
	}

	@Override
	public void initStage(Stage stage)
	{
		final Styleable styleable = DI.instance().get(Styleable.class);
		styleable.applyToStage(stage);

		stage.initModality(Modality.WINDOW_MODAL);

		stage.setTitle(Localization.getString(Strings.UI_DIALOG_NEW_PROJECT_TITLE));
		stage.setWidth(560);
		stage.setHeight(380);

		stage.setMinWidth(560);
		stage.setMinHeight(380);

		stage.setMaxWidth(560);
	}

	public Optional<ProjectMetadata> showAndWait()
	{
		getStageContainer().ifPresent(NVCStage::showAndWait);
		return Optional.ofNullable(project);
	}

	@FXML
	private void finishButtonHandler(ActionEvent event)
	{
		final String name = nameTextField.getText();
		final int numberOfHorizontalPads = spinnerNumberOfHorizontalPads.getValue();
		final int numberOfVerticalPads = spinnerNumberOfVerticalPads.getValue();

		try
		{
			project = client.addProject(name, numberOfHorizontalPads, numberOfVerticalPads);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			showErrorMessage(e.getMessage());
		}

		Platform.runLater(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event)
	{
		getStageContainer().ifPresent(NVCStage::close);
	}
}
