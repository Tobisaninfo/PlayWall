package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.cell.ProjectCell;
import de.tobias.playwall.client.viewcontroller.dialog.ProjectNewDialog;
import de.tobias.playwall.client.viewcontroller.main.MainViewController;
import de.tobias.playwall.client.viewcontroller.style.Styleable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Optional;

import static de.thecodelabs.utils.util.Localization.getString;

@Getter(AccessLevel.PACKAGE)
public class LaunchDialog extends NVC
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

	private final Client client;

	public LaunchDialog(Stage stage, Client client)
	{
		this.client = client;
		load("de/tobias/playwall/client/view", "LaunchDialog", Localization.getBundle());
		applyViewControllerToStage(stage);
	}

	@Override
	public void init()
	{
		final App app = ApplicationUtils.getApplication();

		// Setup launch screen labels and image
		infoLabel.setText(getString(Strings.UI_DIALOG_LAUNCH_INFO, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		openButton.setDisable(true);
		deleteButton.setDisable(true);

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_PLACEHOLDER_PROJECT)));
		projectListView.setCellFactory(_ -> new ProjectCell());

		// List selection listener
		projectListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
			openButton.setDisable(c == null);
			deleteButton.setDisable(c == null);
		});

		// Mouse Double Click on list
		projectListView.setOnMouseClicked(mouseEvent -> {
			if(mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
					mouseEvent.getClickCount() == 2 &&
					!projectListView.getSelectionModel().isEmpty())
			{
				onOpenButton();
			}
		});

		Worker.runLater(this::fetchProjects);
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
		alert.setTitle(getString(Strings.UI_DIALOG_PROJECT_DELETE_TITLE, selectedProject.name()));
		alert.setContentText(getString(Strings.UI_DIALOG_PROJECT_DELETE_CONTENT, selectedProject.name()));
		alert.initOwner(getContainingWindow());
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
		{
			// TODO show progress indicator
			try
			{
				client.deleteProject(selectedProject.id());
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
		final ProjectNewDialog dialog = new ProjectNewDialog(getContainingWindow(), client);
		final Optional<ProjectMetadata> projectOptional = dialog.showAndWait();
		if(projectOptional.isPresent())
		{
			fetchProjects();
		}
	}

	@FXML
	private void onOpenButton()
	{
		try
		{
			final Project project = client.launchProject(getSelectedProject().id());
			Logger.info("Launched project " + project.metadata().name());

			new MainViewController(nvc -> {
				getStageContainer().ifPresent(NVCStage::close);
				nvc.showStage();
				((MainViewController) nvc).openProject(project);
			});
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			showErrorMessage(e.getMessage());
		}
	}

	private void fetchProjects()
	{
		Platform.runLater(() -> {
			try
			{
				projectListView.getItems().setAll(client.getProjects());
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

	@Override
	public void initStage(Stage stage)
	{
		final Styleable styleable = DI.instance().get(Styleable.class);
		styleable.applyToStage(stage);

		stage.setTitle(getString(Strings.UI_DIALOG_LAUNCH_TITLE));
		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(400);
		stage.centerOnScreen();
		stage.show();
	}
}
