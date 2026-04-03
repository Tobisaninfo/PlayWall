package de.tobias.playwall.client.domain.project.view.list;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.*;
import de.tobias.playwall.client.domain.project.view.ProjectDeleteDialog;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.view.settings.BaseProgramSettingsViewController;
import de.tobias.playwall.client.domain.settings.view.settings.ProgramSettingsViewController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.MimeType;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.ViewControllerBase;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.PlayWallButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.thecodelabs.utils.util.Localization.getString;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "ProjectListView")
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Slf4j
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

	@FXML
	@Getter
	private PlayWallButton newProjectButton;

	@FXML
	@Getter
	private PlayWallButton openSettingsButton;

	@FXML
	@Getter
	private PlayWallButton openButton;

	@FXML
	@Getter
	private PlayWallButton deleteButton;

	private final App app;
	private final FluentClient client;
	private final ClientProjectController projectController;
	private final ClientSettingsController settingsController;
	private final CommandLineOptions commandLineOptions;
	private final UpdateMessageEventHandler updateMessageEventHandler;
	private final FileChooserWrapper fileChooserWrapper;
	private final ErrorAlertBuilder errorAlertBuilder;

	private AllProjectsInfo allProjectsInfo;

	@Override
	public void init()
	{
		// Setup launch screen labels and image
		infoLabel.setText(getString(Strings.UI_DIALOG_LAUNCH_INFO, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

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
	public void onDeleteButton()
	{
		final ProjectMetadata selectedProject = getSelectedProject();
		if(selectedProject == null)
		{
			return;
		}

		final Alert alert = new ProjectDeleteDialog(selectedProject, getContainingWindow());
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(_ ->
		{
			try
			{
				client.project(selectedProject.getId()).delete();
				fetchProjects();
			}
			catch(PlayWallApiException e)
			{
				log.error("Cannot delete project", e);
				showErrorMessage(e.getMessage());
			}
		});
	}

	@FXML
	public void onNewProjectButton()
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
	public void onImportProjectButton()
	{
		final MimeType mimeType = MimeType.APPLICATION_JSON;
		fileChooserWrapper.setExtensionFilter(List.of(mimeType.toExtensionFilter()));
		final Optional<Path> pathOptional = fileChooserWrapper.showOpenFile(getContainingWindow());
		if(pathOptional.isEmpty())
		{
			return;
		}
		try
		{
			final byte[] bytes = Files.readAllBytes(pathOptional.get());
			final UUID uuid = client.projects().importProject(new ProjectFile(mimeType.getMimeTypeValue(), bytes));
			openProject(uuid);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot import project", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_IMPORT), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}

	}

	@FXML
	public void onOpenButton()
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
			controller.updateMenuRecentProjects(allProjectsInfo);
			closeStage();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot open project", e);
			showErrorMessage(e.getMessage());
		}
	}

	@FXML
	public void onOpenSettingsButton()
	{
		final ProgramSettingsViewController programSettingsViewController = AppContextHolder.getInstance().get(ProgramSettingsViewController.class);
		programSettingsViewController.showAndWait(new BaseProgramSettingsViewController.Param(settingsController.getSettings()), getContainingWindow());
	}

	void fetchProjects()
	{
		Platform.runLater(() -> {
			try
			{
				allProjectsInfo = client.projects().list();
				projectListView.getItems().setAll(allProjectsInfo.getAllProjectsMetadata());

				final List<UUID> recentProjectIds = allProjectsInfo.getRecentProjectIds();

				if(settingsController.getSettings().isAutoLoadLatestProjectOnStart() && !recentProjectIds.isEmpty())
				{
					projectListView.getItems().stream()
							.filter(p -> p.getId().equals(recentProjectIds.getFirst()))
							.findFirst()
							.ifPresent(p -> openProject(p.getId()));
				}

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
				log.error("Cannot fetch projects", e);
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
