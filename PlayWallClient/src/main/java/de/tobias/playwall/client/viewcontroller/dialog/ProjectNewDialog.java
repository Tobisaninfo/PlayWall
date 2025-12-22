package de.tobias.playwall.client.viewcontroller.dialog;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.AppIconProvider;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.settings.SettingsEntry;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.viewcontroller.ModalBaseNVC;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.util.Optional;

@ViewController(path = "de/tobias/playwall/client/view/dialog", view = "NewProjectDialog")
public class ProjectNewDialog extends ModalBaseNVC<ProjectMetadata>
{
	private static final int MIN_NUMBER_OF_PADS_PER_AXIS = 3;
	private static final int MAX_NUMBER_OF_PADS_PER_AXIS = 10;

	@FXML
	private VBox root;

	private final FluentClient client;

	private ProjectMetadata project;

	private TextField textFieldName;

	private Spinner<Integer> spinnerNumberOfHorizontalPads;
	private Spinner<Integer> spinnerNumberOfVerticalPads;

	@InjectConstructor
	public ProjectNewDialog(FluentClient client)
	{
		this.client = client;
	}

	@Override
	public void init()
	{
		final SettingsEntry settingsEntryName = new SettingsEntry(FontAwesomeType.PEN_TO_SQUARE_SOLID, Localization.getString("ui.dialog.project.create.label.name"), 200);
		textFieldName = new TextField();
		settingsEntryName.setContent(textFieldName);

		final SettingsEntry settingsEntryNumberOfTiles = new SettingsEntry(FontAwesomeType.TABLE_SOLID, Localization.getString("ui.dialog.project.create.label.numberOfPads"), 200);

		spinnerNumberOfHorizontalPads = new Spinner<>(MIN_NUMBER_OF_PADS_PER_AXIS, MAX_NUMBER_OF_PADS_PER_AXIS, 6);
		final HBox hboxHorizontalPads = createHboxNumberOfPads("ui.dialog.project.create.label.numberOfHorizontalPads", spinnerNumberOfHorizontalPads);
		spinnerNumberOfVerticalPads = new Spinner<>(MIN_NUMBER_OF_PADS_PER_AXIS, MAX_NUMBER_OF_PADS_PER_AXIS, 5);
		final HBox hboxVerticalPads = createHboxNumberOfPads("ui.dialog.project.create.label.numberOfVerticalPads", spinnerNumberOfVerticalPads);

		final VBox vboxNumberOfPads = new VBox(hboxHorizontalPads, hboxVerticalPads);
		vboxNumberOfPads.setSpacing(ViewConstants.DEFAULT_SPACING);
		vboxNumberOfPads.setAlignment(Pos.CENTER_LEFT);
		settingsEntryNumberOfTiles.setContent(vboxNumberOfPads);

		final SettingsPage settingsPage = new SettingsPage(List.of(
				settingsEntryName,
				new Separator(),
				settingsEntryNumberOfTiles
		));
		settingsPage.setButtonSaveText(Localization.getString("ui.dialog.project.create.button.finish"));
		settingsPage.setOnSaveAction(this::finishButtonHandler);
		settingsPage.setOnCancelAction(this::cancelButtonHandler);

		root.getChildren().add(settingsPage);
	}

	private HBox createHboxNumberOfPads(String key, Spinner<Integer> spinner)
	{
		final Label labelHorizontalPads = new Label(Localization.getString(key));
		labelHorizontalPads.setPrefWidth(75);
		labelHorizontalPads.setMinWidth(75);

		final HBox hboxHorizontalPads = new HBox(labelHorizontalPads, spinner);
		hboxHorizontalPads.setSpacing(ViewConstants.DEFAULT_SPACING);
		hboxHorizontalPads.setAlignment(Pos.CENTER_LEFT);

		return hboxHorizontalPads;
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
		// TODO: do not allow empty text
		final String name = textFieldName.getText();
		final int numberOfHorizontalPads = spinnerNumberOfHorizontalPads.getValue();
		final int numberOfVerticalPads = spinnerNumberOfVerticalPads.getValue();

		try
		{
			project = client.projects().add(name, numberOfHorizontalPads, numberOfVerticalPads);
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

	/* TODO
	 * - FXML vs. code
	 * - css vs. code
	 * - font
	 */
}
