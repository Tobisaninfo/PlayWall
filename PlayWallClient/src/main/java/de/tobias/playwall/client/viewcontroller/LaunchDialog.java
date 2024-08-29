package de.tobias.playwall.client.viewcontroller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.net.ClientWebSocketHandler;
import de.tobias.playwall.client.net.responsehandler.ProjectResponseHandler;
import de.tobias.playwall.client.net.responsehandler.ResponseHandlerService;
import de.tobias.playwall.client.project.ProjectReference;
import de.tobias.playwall.client.project.ProjectReferenceMock;
import de.tobias.playwall.client.viewcontroller.cell.ProjectCell;
import de.tobias.playwall.common.net.project.ProjectEventMessageType;
import de.tobias.playwall.common.net.project.ProjectMessage;
import de.tobias.playwall.common.utils.MapUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static de.thecodelabs.utils.util.Localization.getString;
import static de.tobias.playwall.common.utils.MapUtils.entry;

public class LaunchDialog extends NVC implements ProjectResponseHandler
{
	static final String IMAGE = "de/tobias/playwall/client/logo/Logo-large.png";

	@FXML
	private Label infoLabel;
	@FXML
	private ImageView imageView;

	@FXML
	private ListView<ProjectReference> projectListView;

	@FXML
	private Button newProjectButton;
	@FXML
	private Button importProjectButton;

	@FXML
	private Button openButton;
	@FXML
	private Button deleteButton;

	public LaunchDialog(Stage stage)
	{
		load("de/tobias/playwall/client/view", "LaunchDialog", Localization.getBundle());
		applyViewControllerToStage(stage);
	}

	@Override
	public void init()
	{
		App app = ApplicationUtils.getApplication();

		// Setup launchscreen labels and image
		infoLabel.setText(getString(Strings.UI_DIALOG_LAUNCH_INFO, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		openButton.setDisable(true);
		deleteButton.setDisable(true);

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
		});

		Worker.runLater(() -> {
			ResponseHandlerService.getInstance().addResponseHandler(ProjectEventMessageType.LIST_PROJECTS, this);

			ClientWebSocketHandler socket = ClientWebSocketHandler.getInstance();
			socket.connect(MapUtils.create(entry("clientId", UUID.randomUUID().toString())));
			socket.send(new ProjectMessage(UUID.randomUUID(), ProjectEventMessageType.LIST_PROJECTS));
		});
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

	@Override
	public void handleResponse(ProjectMessage response)
	{
		final ArrayList<String> list = response.getArray(ProjectEventMessageType.ListProjectsProperties.PROJECTS, ArrayList.class);

		final List<ProjectReferenceMock> projects = list.stream()
				.map(s -> new ProjectReferenceMock(UUID.randomUUID(), s))
				.sorted(Comparator.comparing(ProjectReferenceMock::getName))
				.toList();

		projectListView.getItems().setAll(projects);
	}
}
