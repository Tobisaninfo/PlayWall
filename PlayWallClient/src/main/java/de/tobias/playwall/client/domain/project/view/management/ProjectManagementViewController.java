package de.tobias.playwall.client.domain.project.view.management;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.ProjectFile;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.ProjectDeleteDialog;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.list.ProjectListViewController;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.MimeType;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.ParamDialogBase;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view/project", view = "ProjectManagementView")
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Slf4j
public class ProjectManagementViewController extends ParamDialogBase<ProjectManagementViewController.Param>
{
	@AllArgsConstructor
	public static class Param
	{
		private final MainViewController mainViewController;
	}

	@FXML
	private TextField searchTextField;

	@FXML
	private ListView<ProjectMetadata> projectListView;

	@FXML
	private Button importButton;
	@FXML
	private Button newButton;

	private final FluentClient client;
	private final ClientProjectController projectController;
	private final ErrorAlertBuilder errorAlertBuilder;
	private final FileChooserWrapper fileChooserWrapper;

	private MainViewController mainViewController;

	@Override
	protected void init()
	{
		super.init();

		this.searchTextField.setPromptText(Localization.getString("ui.project.management.search.prompt"));
		this.projectListView.setCellFactory(_ -> new ProjectManagementCell(projectController, this::onProjectCellAction));

		projectListView.setOnMouseClicked(mouseEvent -> {
			if(mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
			   mouseEvent.getClickCount() == 2 &&
			   !projectListView.getSelectionModel().isEmpty())
			{
				closeStage();
				mainViewController.closeCurrentProjectAndOpenProject(getSelectedProject().getId());
			}
		});

		fetchProjects();
	}

	private void fetchProjects()
	{
		try
		{
			projectListView.getItems().setAll(client.projects().list().getAllProjectsMetadata());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot fetch project", e);
			showErrorMessage(e.getMessage());
		}
	}

	private void onProjectCellAction(ProjectManagementCell.ProjectManagementCellAction action, ProjectMetadata project)
	{
		switch(action)
		{
			case DELETE -> onDeleteProject(project);
			case EXPORT -> onExportProject(project);
		}
	}

	private void onExportProject(ProjectMetadata project)
	{
		try
		{
			final ProjectFile export = client.project(project.getId()).export();

			final MimeType mimeType = MimeType.getByMimeType(export.mimetype());
			fileChooserWrapper.setExtensionFilter(List.of(mimeType.toExtensionFilter()));
			fileChooserWrapper.setInitialFilename(project.getName() + "." + mimeType.getExtension());
			final Optional<Path> pathOptional = fileChooserWrapper.showSaveFile(getContainingWindow());
			if(pathOptional.isEmpty())
			{
				return;
			}
			final Path path = pathOptional.get();
			Files.write(path, export.data());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot export project", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_EXPORT), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
		catch(IOException e)
		{
			log.error("Cannot write file", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_EXPORT), e.getMessage(), getContainingWindow()).showAndWait();
		}
	}

	private void onDeleteProject(ProjectMetadata project)
	{
		final boolean isActiveProject = isActiveProject(project);
		if(isActiveProject && projectController.isAtLeastOnePadPlaying())
		{
			showErrorMessage(Localization.getString(Strings.UI_EXIT_WARNING_PLAYING));
			return;
		}

		final Alert alert = new ProjectDeleteDialog(project, getContainingWindow());
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(_ ->
		{
			try
			{

				if(isActiveProject)
				{
					closeStage();
					mainViewController.closeStage();
					AppContextHolder.getInstance().get(ProjectListViewController.class).showStage();
				}

				client.project(project.getId()).delete();
				fetchProjects();
			}
			catch(PlayWallApiException e)
			{
				log.error("Cannot delete project", e);
				showErrorMessage(e.getMessage());
			}
		});
	}

	@Override
	public void initParameter(Param param)
	{
		mainViewController = param.mainViewController;
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		stage.setTitle(Localization.getString("ui.project.management.title"));
		stage.setMinHeight(500);
		stage.setMinWidth(400);
	}

	private ProjectMetadata getSelectedProject()
	{
		return projectListView.getSelectionModel().getSelectedItem();
	}

	@FXML
	void onImportButton()
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
			client.projects().importProject(new ProjectFile(mimeType.getMimeTypeValue(), bytes));
			fetchProjects();
		}
		catch(IOException e)
		{
			log.error("Cannot read file", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_IMPORT), e.getMessage(), getContainingWindow()).showAndWait();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot import project", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_IMPORT), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	private void onNewButton()
	{
		final ProjectNewDialog dialog = AppContextHolder.getInstance().get(ProjectNewDialog.class);
		final Optional<ProjectMetadata> projectOptional = dialog.showAndWait(getContainingWindow());
		projectOptional.ifPresent(projectMetadata -> {
			closeStage();
			mainViewController.closeCurrentProjectAndOpenProject(projectMetadata.getId());
		});
	}

	private boolean isActiveProject(ProjectMetadata project)
	{
		return projectController.getProject().getMetadata().getId().equals(project.getId());
	}
}
