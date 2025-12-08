package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.launch.ServerLauncher;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Arrays;

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
	protected void initStage(Stage stage)
	{
		stage.initStyle(StageStyle.UNDECORATED);
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
			client.connectWithRetries(60, this::updateLoadingLabel);

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
