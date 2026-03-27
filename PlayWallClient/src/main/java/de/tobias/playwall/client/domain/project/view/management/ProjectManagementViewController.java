package de.tobias.playwall.client.domain.project.view.management;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.ParamDialogBase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	private MainViewController mainViewController;

	@Override
	protected void init()
	{
		super.init();

		this.searchTextField.setPromptText(Localization.getString("ui.project.management.search.prompt"));
		this.projectListView.setCellFactory(_ -> new ProjectManagementCell(projectController));
		projectListView.setOnMouseClicked(mouseEvent -> {
			if(mouseEvent.getButton().equals(MouseButton.PRIMARY) &&
			   mouseEvent.getClickCount() == 2 &&
			   !projectListView.getSelectionModel().isEmpty())
			{
				closeStage();
				mainViewController.closeCurrentProjectAndOpenProject(getSelectedProject().getId());
			}
		});

		try
		{
			projectListView.getItems().setAll(client.projects().list().getAllProjectsMetadata());
		}
		catch(PlayWallApiException e)
		{
			throw new RuntimeException(e); // TODO
		}
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
	private void onImportButton()
	{

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
}
