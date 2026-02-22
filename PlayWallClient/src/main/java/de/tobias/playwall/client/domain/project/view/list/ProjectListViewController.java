package de.tobias.playwall.client.domain.project.view.list;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.view.settings.ProgramSettingsViewController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.ViewControllerBase;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import static de.thecodelabs.utils.util.Localization.getString;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "ProjectListView")
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class ProjectListViewController extends ViewControllerBase
{
	static final String IMAGE = "de/tobias/playwall/client/logo/icon_large.png";

	@FXML
	private Label infoLabel;
	@FXML
	private ImageView imageView;

	@FXML
	private ListView<ProjectMetadata> projectListView;

	@FXML
	private VBox buttonBox;
	@FXML
	private HBox projectButtonBox;

	@Getter
	private PlayWallButton newProjectButton;

	@Getter
	private PlayWallButton openButton;

	@Getter
	private PlayWallButton deleteButton;

	private final App app;
	private final FluentClient client;
	private final ClientProjectController projectController;
	private final ClientSettingsController settingsController;
	private final CommandLineOptions commandLineOptions;
	private final UpdateMessageEventHandler updateMessageEventHandler;

	@Override
	public void init()
	{
		// Setup launch screen labels and image
		infoLabel.setText(getString(Strings.UI_DIALOG_LAUNCH_INFO, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		newProjectButton = new PlayWallButton(Localization.getString("launch.button.new"), FontAwesomeType.FOLDER_PLUS_SOLID);
		newProjectButton.setId("newProjectButton");
		newProjectButton.setMaxWidth(Double.MAX_VALUE);
		newProjectButton.setOnAction(this::onNewProjectButton);

		final PlayWallButton importProjectButton = new PlayWallButton(Localization.getString("launch.button.import"), FontAwesomeType.FILE_IMPORT_SOLID);
		importProjectButton.setId("importProjectButton");
		importProjectButton.setMaxWidth(Double.MAX_VALUE);
		importProjectButton.setDisable(true);

		final HBox box = new HBox(ViewConstants.DEFAULT_SPACING);
		box.setMaxWidth(Double.MAX_VALUE);
		box.getChildren().addAll(newProjectButton, importProjectButton);
		HBox.setHgrow(newProjectButton, Priority.ALWAYS);
		HBox.setHgrow(importProjectButton, Priority.ALWAYS);

		final PlayWallButton openSettingsButton = new PlayWallButton(Localization.getString("launch.button.settings"), FontAwesomeType.GEAR_SOLID);
		openSettingsButton.setId("openSettingsButton");
		openSettingsButton.setMaxWidth(Double.MAX_VALUE);
		openSettingsButton.setOnAction(this::onOpenSettingsButton);

		buttonBox.getChildren().addAll(box, openSettingsButton);

		deleteButton = new PlayWallButton(Localization.getString("launch.button.delete"), FontAwesomeType.TRASH_CAN_SOLID);
		deleteButton.setId("deleteButton");
		deleteButton.setMaxWidth(Double.MAX_VALUE);
		deleteButton.setMinHeight(35.0);
		deleteButton.setOnAction(this::onDeleteButton);
		deleteButton.setDisable(true);

		openButton = new PlayWallButton(Localization.getString("launch.button.open"), FontAwesomeType.UP_RIGHT_FROM_SQUARE_SOLID);
		openButton.setId("deleteButton");
		openButton.setMaxWidth(Double.MAX_VALUE);
		openButton.setMinHeight(35.0);
		openButton.setOnAction(this::onOpenButton);
		openButton.setDisable(true);
		projectButtonBox.getChildren().addAll(deleteButton, openButton);
		HBox.setHgrow(deleteButton, Priority.ALWAYS);
		HBox.setHgrow(openButton, Priority.ALWAYS);

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_PLACEHOLDER_PROJECT)));
		projectListView.setCellFactory(_ -> new ProjectCell());

		// List selection listener
		projectListView.getSelectionModel().selectedItemProperty().addListener((_, _, c) -> {
			openButton.setDisable(c == null);
			deleteButton.setDisable(c == null);
		});

		// Mouse Double Click on the list
		projectListView.setOnMouseClicked(mouseEvent -> {
			if(mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
					mouseEvent.getClickCount() == 2 &&
					!projectListView.getSelectionModel().isEmpty())
			{
				openProject(getSelectedProject().getId());
			}
		});

		Worker.runLater(this::fetchProjects);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		stage.setTitle(getString(Strings.UI_DIALOG_LAUNCH_TITLE));
		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(400);
		stage.centerOnScreen();
	}

	@FXML
	private void onDeleteButton(ActionEvent event)
	{
		final ProjectMetadata selectedProject = getSelectedProject();
		if(selectedProject == null)
		{
			return;
		}

		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(getString(Strings.UI_DIALOG_PROJECT_DELETE_TITLE, selectedProject.getName()));
		alert.setContentText(getString(Strings.UI_DIALOG_PROJECT_DELETE_CONTENT, selectedProject.getName()));
		alert.initOwner(getContainingWindow());
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(_ ->
		{
			try
			{
				client.project(selectedProject.getId()).delete();
				fetchProjects();
			}
			catch(PlayWallApiException e)
			{
				Logger.error(e.getMessage());
				showErrorMessage(e.getMessage());
			}
		});
	}

	@FXML
	private void onNewProjectButton(ActionEvent event)
	{
		final ProjectNewDialog dialog = AppContextHolder.getInstance().get(ProjectNewDialog.class);
		final Optional<ProjectMetadata> projectOptional = dialog.showAndWait(getContainingWindow());
		if(projectOptional.isPresent())
		{
			fetchProjects();
			openProject(projectOptional.get().getId());
		}
	}

	@FXML
	private void onOpenButton(ActionEvent event)
	{
		openProject(getSelectedProject().getId());
	}

	private void openProject(UUID id)
	{
		try
		{
			final Project project = client.project(id).get();

			final MainViewController controller = AppContextHolder.getInstance().get(MainViewController.class);
			controller.showStage();
			controller.showProject(project);
			closeStage();
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			showErrorMessage(e.getMessage());
		}
	}

	@FXML
	private void onOpenSettingsButton(ActionEvent event)
	{
		final ProgramSettingsViewController programSettingsViewController = AppContextHolder.getInstance().get(ProgramSettingsViewController.class);
		programSettingsViewController.showAndWait(new ProgramSettingsViewController.Param(settingsController.getSettings()), getContainingWindow());
	}

	void fetchProjects()
	{
		Platform.runLater(() -> {
			try
			{
				projectListView.getItems().setAll(client.projects().list());

				if(commandLineOptions.hasOption(CommandLineOptions.PROJECT))
				{
					final String projectName = commandLineOptions.getOptionValue(CommandLineOptions.PROJECT)
							.toLowerCase()
							.strip();
					projectListView.getItems().stream()
							.filter(p -> p.getName().toLowerCase().equals(projectName))
							.findFirst()
							.ifPresent(p -> openProject(p.getId()));
				}
			}
			catch(PlayWallApiException e)
			{
				Logger.error(e.getMessage());
				showErrorMessage(e.getMessage());
			}
		});
	}

	/**
	 * Returns the selected project from the list view
	 *
	 * @return Project
	 */
	private ProjectMetadata getSelectedProject()
	{
		return projectListView.getSelectionModel().getSelectedItem();
	}
}
