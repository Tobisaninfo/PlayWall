package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.*;
import de.tobias.playwall.client.launch.ServerLaunchException;
import de.tobias.playwall.client.launch.ServerLauncher;
import de.tobias.playwall.client.net.Client;
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
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "ApplicationLoadingView", applyToStage = false)
public class ApplicationLoadingViewController extends BaseNVC
{
	@SuppressWarnings("java:S2094")
	private abstract static sealed class LoadingResult
	{
	}

	private static final class SuccessResult extends LoadingResult
	{
	}

	@AllArgsConstructor
	private static final class FailureResult extends LoadingResult
	{
		String errorMessage;
		Throwable throwable;
		boolean showLogFolderButton;
	}

	@FXML
	private Label titleLabel;
	@FXML
	private Label versionLabel;
	@FXML
	private Label loadingLabel;

	private final App app;
	private final Client client;
	private final ServerLauncher serverLauncher;

	@InjectConstructor
	ApplicationLoadingViewController(App app, Client client, ServerLauncher serverLauncher)
	{
		this.app = app;
		this.client = client;
		this.serverLauncher = serverLauncher;
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
		super.initStage(stageContainer, stage);
		stageContainer.initStyle(StageStyle.UNDECORATED);
	}

	@PostConstruct
	void initializeApplication()
	{
		Worker.runLater(() -> {
			LoadingResult result = null;
			final String[] args = app.getProgramArgs();
			if(!(args != null && args.length != 0 && Arrays.binarySearch(args, "--standalone") >= 0))
			{
				Runtime.getRuntime().addShutdownHook(new Thread(serverLauncher::stopServer));
				try
				{
					serverLauncher.launchServer();
				}
				catch(ServerLaunchException.NotFoundException e)
				{
					result = new FailureResult(Localization.getString("ui.application_loading.error.server.not_found", e.getPath()), e, false);
				}
				catch(ServerLaunchException.PortInUseException e)
				{
					result = new FailureResult(Localization.getString("ui.application_loading.error.server.port_in_use", 10023), e, false);
				}
				catch(ServerLaunchException.GenericStartupException e)
				{
					result = new FailureResult(Localization.getString(Localization.getString("ui.application_loading.error.server.generic", e.getMessage()), 10023), e, true);
				}
			}
			if(result != null)
			{
				try
				{
					client.connectWithRetries(60, this::updateLoadingLabel);
					result = new SuccessResult();
				}
				catch(Exception e)
				{
					result = new FailureResult(Localization.getString("ui.application_loading.error.connect", e.getMessage()), e, false);
				}
			}

			switch(Objects.requireNonNull(result))
			{
				case FailureResult failureResult ->
				{
					Logger.error(failureResult.throwable);
					Platform.runLater(() -> {
						final Alert alert = Alerts.getInstance().createAlert(Alert.AlertType.ERROR, null, failureResult.errorMessage);
						alert.getButtonTypes().clear();
						if(failureResult.showLogFolderButton)
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
					});
				}
				case SuccessResult _ -> Platform.runLater(() -> {
					closeStage();
					final LaunchDialog dialog = AppContextHolder.getInstance().get(LaunchDialog.class);
					dialog.showStage();
				});
			}
		});
	}

	private void updateLoadingLabel(int currentTry, int maximumNumberOfTries)
	{
		Platform.runLater(() -> loadingLabel.setText(Localization.getString("ui.application_loading.label", currentTry, maximumNumberOfTries)));
	}
}
