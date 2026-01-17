package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVCStage;
import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.client.viewcontroller.cell.ProjectCell;
import de.tobias.playwall.client.viewcontroller.dialog.ProjectNewDialog;
import de.tobias.playwall.client.viewcontroller.main.MainViewController;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import static de.thecodelabs.utils.util.Localization.getString;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "LaunchDialog")
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class LaunchDialog extends ViewControllerBase
{
	static final String IMAGE = "de/tobias/playwall/client/logo/icon_large.png";

	@FXML
	private Label infoLabel;
	@FXML
	private ImageView imageView;

	@FXML
	private ListView<ProjectMetadata> projectListView;

	@FXML
	private Button newProjectButton;
	@FXML
	private Button importProjectButton;

	@FXML
	private Button openButton;
	@FXML
	private Button deleteButton;

	private final App app;
	private final FluentClient client;
	private final ClientProjectController projectController;
	private final CommandLineOptions commandLineOptions;
	private final UpdateMessageEventHandler updateMessageEventHandler;

	@Override
	public void init()
	{
		// Setup launch screen labels and image
		infoLabel.setText(getString(Strings.UI_DIALOG_LAUNCH_INFO, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		openButton.setDisable(true);
		deleteButton.setDisable(true);

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

		updateMessageEventHandler.registerListener(new UpdateMessageEventListener<PadLoadedUpdate>()
		{
			@Override
			public void onUpdateMessage(PadLoadedUpdate message)
			{
				if(message.getDurationMillis() != null)
				{
					final Duration duration = Duration.millis(message.getDurationMillis());
					projectController.getPadController(message.getPadId()).setDuration(duration);
				}
			}

			@Override
			public Class<PadLoadedUpdate> getMessageClass()
			{
				return PadLoadedUpdate.class;
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
	private void onDeleteButton()
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
	private void onNewProjectButton()
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
	private void onOpenButton()
	{
		openProject(getSelectedProject().getId());
	}

	private void openProject(UUID id)
	{
		try
		{
			final Project project = client.project(id).get();
			this.projectController.loadProject(project);
			client.project(id).load();
			Logger.info("Launched project " + project.getMetadata().getName());

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
