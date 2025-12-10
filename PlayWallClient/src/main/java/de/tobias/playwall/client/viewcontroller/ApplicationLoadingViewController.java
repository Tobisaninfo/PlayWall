package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.*;
import de.tobias.playwall.client.launch.ServerLauncher;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Arrays;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view", view = "ApplicationLoadingView", applyToStage = false)
public class ApplicationLoadingViewController extends BaseNVC
{
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
		stageContainer.initStyle(StageStyle.UNDECORATED);
		styleable.applyToStage(stage);
	}

	@PostConstruct
	void initializeApplication()
	{
		Worker.runLater(() -> {
			final String[] args = app.getProgramArgs();
			if(!(args != null && args.length != 0 && Arrays.binarySearch(args, "--standalone") >= 0))
			{
				Runtime.getRuntime().addShutdownHook(new Thread(serverLauncher::stopServer));
				serverLauncher.launchServer();
			}
			try
			{
				client.connectWithRetries(60, this::updateLoadingLabel);
			}
			catch(Exception e)
			{
				Logger.error(e);
				Platform.runLater(() -> {
					showErrorMessage(Localization.getString("ui.application_loading.error", e.getMessage()));
					if(AppContextHolder.getInstance().getEnvironment() != AppContext.Environment.GUI_TESTING)
					{
						System.exit(0);
					}
				});
				return;
			}

			Platform.runLater(() -> {
				closeStage();
				final LaunchDialog dialog = AppContextHolder.getInstance().get(LaunchDialog.class);
				dialog.showStage();
			});
		});
	}

	private void updateLoadingLabel(int currentTry, int maximumNumberOfTries)
	{
		Platform.runLater(() -> loadingLabel.setText(Localization.getString("ui.application_loading.label", currentTry, maximumNumberOfTries)));
	}
}
