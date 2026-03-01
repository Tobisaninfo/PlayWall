package de.tobias.playwall.client.view.launch;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.*;
import de.tobias.playwall.client.domain.project.view.list.ProjectListViewController;
import de.tobias.playwall.client.view.ViewControllerBase;
import de.tobias.playwall.client.view.launch.tasks.ClientConnectLaunchTask;
import de.tobias.playwall.client.view.launch.tasks.FetchProgramSettingsLaunchTask;
import de.tobias.playwall.client.view.launch.tasks.LaunchTask;
import de.tobias.playwall.client.view.launch.tasks.ServerLaunchTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "ApplicationLoadingView", applyToStage = false)
@Slf4j
public class ApplicationLoadingViewController extends ViewControllerBase
{
	@FXML
	private Label titleLabel;
	@FXML
	private Label versionLabel;
	@FXML
	private Label loadingLabel;

	private final App app;
	private final ServerLaunchTask serverLaunchTask;
	private final ClientConnectLaunchTask connectLaunchTask;
	private final FetchProgramSettingsLaunchTask fetchProgramSettingsLaunchTask;

	@InjectConstructor
	ApplicationLoadingViewController(App app, ServerLaunchTask serverLaunchTask, ClientConnectLaunchTask connectLaunchTask, FetchProgramSettingsLaunchTask fetchProgramSettingsLaunchTask)
	{
		this.app = app;
		this.serverLaunchTask = serverLaunchTask;
		this.connectLaunchTask = connectLaunchTask;
		this.fetchProgramSettingsLaunchTask = fetchProgramSettingsLaunchTask;
	}

	@Override
	protected void init()
	{
		titleLabel.setText(app.getInfo().getName());
		versionLabel.setText(app.getInfo().getVersion());
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		stageContainer.initStyle(StageStyle.UNDECORATED);
		styleable.applyToStage(stage);
	}

	@PostConstruct
	void initializeApplication()
	{
		Worker.runLater(() -> {
			LaunchTask.LaunchResult result = new LaunchTask.SuccessResult();

			final List<LaunchTask> tasks = List.of(serverLaunchTask, connectLaunchTask, fetchProgramSettingsLaunchTask);
			for(LaunchTask task : tasks)
			{
				result = task.launch(loadingLabel);
				if(result instanceof LaunchTask.FailureResult)
				{
					break;
				}
			}

			switch(Objects.requireNonNull(result))
			{
				case LaunchTask.FailureResult failureResult ->
				{
					log.error("Cannot load application", failureResult.getThrowable());
					Platform.runLater(() -> showLaunchErrorDialog(failureResult));
				}
				case LaunchTask.SuccessResult _ -> Platform.runLater(() -> {
					closeStage();
					final ProjectListViewController dialog = AppContextHolder.getInstance().get(ProjectListViewController.class);
					dialog.showStage();
				});
			}
		});
	}

	private void showLaunchErrorDialog(LaunchTask.FailureResult failureResult)
	{
		final Alert alert = Alerts.getInstance().createAlert(Alert.AlertType.ERROR, null, failureResult.getErrorMessage());
		alert.getButtonTypes().clear();
		if(failureResult.isShowLogFolderButton())
		{
			alert.getButtonTypes().add(new ButtonType(Localization.getString("ui.button.show_log"), ButtonBar.ButtonData.HELP));
		}
		alert.getButtonTypes().add(new ButtonType(Localization.getString("ui.button.exit"), ButtonBar.ButtonData.OK_DONE));
		getStageContainer().ifPresent(nvcStage -> alert.initOwner(nvcStage.getStage()));
		alert.initModality(Modality.WINDOW_MODAL);
		final Optional<ButtonType> response = alert.showAndWait();
		if(response.filter(button -> button.getButtonData() == ButtonBar.ButtonData.HELP).isPresent())
		{
			NativeApplication.sharedInstance().showFileInFileViewer(app.getPath(PathType.LOG));
		}
		if(AppContextHolder.getInstance().getEnvironment() != AppContext.Environment.GUI_TESTING)
		{
			System.exit(0);
		}
	}
}
