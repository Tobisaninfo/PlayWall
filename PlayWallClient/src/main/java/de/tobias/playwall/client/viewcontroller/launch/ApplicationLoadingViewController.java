package de.tobias.playwall.client.viewcontroller.launch;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.*;
import de.tobias.playwall.client.viewcontroller.ViewControllerBase;
import de.tobias.playwall.client.viewcontroller.LaunchDialog;
import de.tobias.playwall.client.viewcontroller.launch.tasks.ClientConnectLaunchTask;
import de.tobias.playwall.client.viewcontroller.launch.tasks.LaunchTask;
import de.tobias.playwall.client.viewcontroller.launch.tasks.ServerLaunchTask;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "ApplicationLoadingView", applyToStage = false)
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

	@InjectConstructor
	ApplicationLoadingViewController(App app, ServerLaunchTask serverLaunchTask, ClientConnectLaunchTask connectLaunchTask)
	{
		this.app = app;
		this.serverLaunchTask = serverLaunchTask;
		this.connectLaunchTask = connectLaunchTask;
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

			final List<LaunchTask> tasks = List.of(serverLaunchTask, connectLaunchTask);
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
					Logger.error(failureResult.getThrowable());
					Platform.runLater(() -> showLaunchErrorDialog(failureResult));
				}
				case LaunchTask.SuccessResult _ -> Platform.runLater(() -> {
					closeStage();
					final LaunchDialog dialog = AppContextHolder.getInstance().get(LaunchDialog.class);
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
