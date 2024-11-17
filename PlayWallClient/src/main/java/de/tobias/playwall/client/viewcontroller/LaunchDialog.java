package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.model.project.ProjectMetadataDao;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.cell.ProjectCell;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static de.thecodelabs.utils.util.Localization.getString;

public class LaunchDialog extends NVC
{
	static final String IMAGE = "de/tobias/playwall/client/logo/Logo-large.png";

	@FXML
	private Label infoLabel;
	@FXML
	private ImageView imageView;

	@FXML
	private ListView<ProjectMetadataDao> projectListView;

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
		deleteButton.setOnAction(event -> onDeleteButton());

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_PLACEHOLDER_PROJECT)));
		projectListView.setId("list");
		projectListView.setCellFactory(list -> new ProjectCell());

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
				// TODO
				// launchProject(getSelectedProject());
			}
			else if(mouseEvent.getButton().equals(MouseButton.SECONDARY))
			{
				Worker.runLater(() -> {
					client.deleteProject(getSelectedProject(), response -> {
						if(response.isSuccess())
						{
							Logger.debug("Refresh project list");
							fetchProjects();
						}
					});
				});
			}
		});

		Worker.runLater(this::fetchProjects);
	}

	private void onDeleteButton()
	{
		final ProjectMetadataDao selectedProject = getSelectedProject();
		if(selectedProject == null)
		{
			return;
		}

		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setContentText(getString(Strings.UI_DIALOG_PROJECT_MANAGER_DELETE_CONTENT, selectedProject.name()));
		alert.initOwner(getContainingWindow());
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
		{
			// TODO show progress indicator
			client.deleteProject(selectedProject, deleteResponse -> {
				if(deleteResponse.isSuccess())
				{
					fetchProjects();
				}
				else
				{
					showErrorMessage(getString(Strings.ERROR_PROJECT_DELETE, "Error deleting project " + selectedProject.name()));
				}
			});
		});
	}

	private void fetchProjects()
	{
		client.getProjects(projects -> Platform.runLater(() -> projectListView.getItems().setAll(projects)));
	}

	/**
	 * Returns the selected project from the list view
	 *
	 * @return Project
	 */
	private ProjectMetadataDao getSelectedProject()
	{
		return projectListView.getSelectionModel().getSelectedItem();
	}

	@Override
	public void initStage(Stage stage)
	{
		stage.setTitle(getString(Strings.UI_DIALOG_LAUNCH_TITLE));
		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(400);
		stage.centerOnScreen();
		stage.show();
	}
}
