package de.tobias.playwall.client.viewcontroller.main;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.SnackBar;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.AppIconProvider;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.utils.Size;
import de.tobias.playwall.client.viewcontroller.style.ModernStyleSizeHelper;
import de.tobias.playwall.client.viewcontroller.style.Styleable;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainViewController extends NVC
{
	@FXML
	private VBox headerBox;
	@FXML
	private GridPane padGridPane;

	@FXML
	private AnchorPane gridContainer;

	private SnackBar notificationPane;

	private List<PadView> currentPads;

	private Project project;

	public MainViewController(Consumer<NVC> onFinish)
	{
		load("de/tobias/playwall/client/view/main", "MainView", Localization.getBundle(), e ->
		{
			NVCStage stage = e.applyViewControllerToStage();
			stage.addCloseHook(this::closeRequest);

			// Init with existing stage
			onFinish.accept(e);
		});
	}

	@Override
	public void init()
	{
		padGridPane.getStyleClass().add("pad-grid");

		notificationPane = new SnackBar(padGridPane, new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE));
		final Action closeAction = new Action(event -> notificationPane.hide());
		closeAction.setGraphic(new FontIcon(FontAwesomeType.TIMES));
		notificationPane.getActions().add(closeAction);
		notificationPane.setCloseButtonVisible(false);
		gridContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);
	}

	@Override
	public void initStage(Stage stage)
	{
		final Styleable styleable = DI.instance().get(Styleable.class);
		styleable.applyToStage(stage);

		stage.getIcons().add(DI.instance().get(AppIconProvider.class).getStageIcon());
		stage.setTitle(getWindowTitle("-", "-"));
		stage.show();
	}

	private static String getWindowTitle(String projectName, String profileName)
	{
		return Localization.getString("ui.window.main.title", projectName, profileName);
	}

	private boolean closeRequest()
	{
		return true;
	}

	private Size computeMinStageSize(int columns, int rows)
	{
		double minWidth = ModernStyleSizeHelper.getMinWidth(columns);
		double minHeight = ModernStyleSizeHelper.getMinHeight(rows);

		if(minWidth < 500)
		{
			minWidth = 500;
		}

		return new Size(minWidth, minHeight + (OS.isMacOS() ? 100 : 150));
	}

	private void updateWindowProperties(Project project)
	{
		final Stage stage = getStage();
		final Size minSize = computeMinStageSize(project.metadata().numberOfHorizontalPads(), project.metadata().numberOfVerticalPads());

		stage.setMinWidth(minSize.width());
		stage.setMinHeight(minSize.height());

		stage.setTitle(getWindowTitle(project.metadata().name(), "-")); // TODO: Profile name
	}

	private Stage getStage()
	{
		return getStageContainer().map(NVCStage::getStage).orElseThrow();
	}

	// Project handling

	public void openProject(Project project)
	{
		this.project = project;

		updateWindowProperties(project);
		initializePadViews(project.metadata().numberOfHorizontalPads(), project.metadata().numberOfVerticalPads());

		showPage(0);
	}

	private void initializePadViews(int columns, int rows)
	{
		currentPads = new ArrayList<>();

		// Table
		padGridPane.getColumnConstraints().clear();
		double xPercentage = 1.0 / columns;
		for (int i = 0; i < columns; i++) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(xPercentage * 100);
			padGridPane.getColumnConstraints().add(c);
		}

		padGridPane.getRowConstraints().clear();
		double yPercentage = 1.0 / rows;
		for (int i = 0; i < rows; i++) {
			RowConstraints c = new RowConstraints();
			c.setPercentHeight(yPercentage * 100);
			padGridPane.getRowConstraints().add(c);
		}
	}

	public void showPage(int position)
	{
		final Page page = this.project.getPage(position);

	}
}
