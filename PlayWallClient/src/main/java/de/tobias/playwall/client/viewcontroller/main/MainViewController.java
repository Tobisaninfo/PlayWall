package de.tobias.playwall.client.viewcontroller.main;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.thecodelabs.utils.ui.scene.SnackBar;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.utils.Size;
import de.tobias.playwall.client.viewcontroller.BaseNVC;
import de.tobias.playwall.client.viewcontroller.main.desktop.DesktopPadViewProvider;
import de.tobias.playwall.client.viewcontroller.style.ModernStyleSizeHelper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;

import java.util.ArrayList;
import java.util.List;

@ViewController(path = "de/tobias/playwall/client/view/main", view = "MainView")
public class MainViewController extends BaseNVC
{
	@FXML
	private VBox headerBox;
	@FXML
	private GridPane padGridPane;

	@FXML
	private AnchorPane gridContainer;

	private final PadViewProvider padViewProvider;

	private SnackBar notificationPane;

	private final List<PadView> padViews = new ArrayList<>();

	private Project project;

	@InjectConstructor
	MainViewController(DesktopPadViewProvider padViewProvider)
	{
		this.padViewProvider = padViewProvider;
	}

	@Override
	protected void init()
	{
		padGridPane.getStyleClass().add("pad-grid");

		notificationPane = new SnackBar(padGridPane, new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID));
		final Action closeAction = new Action(event -> notificationPane.hide());
		closeAction.setGraphic(new FontIcon(FontAwesomeType.CROSS_SOLID));
		notificationPane.getActions().add(closeAction);
		notificationPane.setCloseButtonVisible(false);
		gridContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);

		headerBox.getChildren().add(createMenu());
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stageContainer.addCloseHook(this::closeRequest);

		stage.setTitle(getWindowTitle("-"));
		stage.show();
	}

	private static String getWindowTitle(String projectName)
	{
		return Localization.getString("ui.window.main.title", projectName);
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

		final int menuAndToolbarHeight = OS.isMacOS() ? 100 : 150;
		return new Size(minWidth, minHeight + menuAndToolbarHeight);
	}

	private void updateWindowProperties(Project project)
	{
		final Stage stage = getStage();
		final Size minSize = computeMinStageSize(project.metadata().numberOfHorizontalPads(), project.metadata().numberOfVerticalPads());

		stage.setMinWidth(minSize.width());
		stage.setMinHeight(minSize.height());

		stage.setTitle(getWindowTitle(project.metadata().name()));
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
		// Table
		padGridPane.getColumnConstraints().clear();
		double xPercentage = 1.0 / columns;
		for(int i = 0; i < columns; i++)
		{
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(xPercentage * 100);
			padGridPane.getColumnConstraints().add(c);
		}

		padGridPane.getRowConstraints().clear();
		double yPercentage = 1.0 / rows;
		for(int i = 0; i < rows; i++)
		{
			RowConstraints c = new RowConstraints();
			c.setPercentHeight(yPercentage * 100);
			padGridPane.getRowConstraints().add(c);
		}

		// Pads - Remove alte PadViews, falls noch welche vorhanden
		if(!padViews.isEmpty())
			removePadViews();

		// Neue PadViews
		for(int y = 0; y < rows; y++)
		{
			for(int x = 0; x < columns; x++)
			{
				final PadView padView = padViewProvider.createNewPadView();
				padGridPane.add(padView.getRootNode(), x, y);
				padViews.add(padView);
			}
		}
	}

	private void removePadViews()
	{
		padViews.forEach(view ->
		{
			padGridPane.getChildren().remove(view.getRootNode());
		});
		padViews.clear();
	}

	public void showPage(int pageNumber)
	{
		final Page page = this.project.getPage(pageNumber);
		final int padNumberPerPage = project.metadata().numberOfHorizontalPads() * project.metadata().numberOfVerticalPads();

		for(int i = 0; i < padNumberPerPage; i++)
		{
			final PadView view = padViews.get(i);
			final Pad pad = page.getPad(i);

			view.updateFromPad(pad);
		}
	}

	private MenuBar createMenu()
	{
		final Menu menuFile = createMenuFile();
		final Menu menuEdit = createMenuEdit();
		final Menu menuView = createMenuView();
		final Menu menuInfo = createMenuInfo();

		// TODO: enable as soon as implemented
		menuFile.getItems().forEach(item -> item.setDisable(true));
		menuView.getItems().forEach(item -> item.setDisable(true));
		menuEdit.getItems().forEach(item -> item.setDisable(true));
		menuInfo.getItems().forEach(item -> item.setDisable(true));

		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuInfo);
		return menuBar;
	}

	private Menu createMenuFile()
	{
		final MenuItem menuItemNewProject = new MenuItem(Localization.getString(Strings.UI_MENU_FILE_NEW_PROJECT), createFontIcon(FontAwesomeType.FOLDER_PLUS_SOLID));
		final Menu menuRecentProject = new Menu(Localization.getString(Strings.UI_MENU_FILE_RECENT_PROJECT), createFontIcon(FontAwesomeType.CLOCK_ROTATE_LEFT_SOLID));
		final MenuItem menuItemManageProject = new MenuItem(Localization.getString(Strings.UI_MENU_FILE_MANAGE_PROJECTS), createFontIcon(FontAwesomeType.FOLDER_TREE_SOLID));
		final MenuItem menuItemSaveProject = new MenuItem(Localization.getString(Strings.UI_MENU_FILE_SAVE_PROJECT), createFontIcon(FontAwesomeType.FLOPPY_DISK_SOLID));
		final MenuItem menuItemProjectSettings = new MenuItem(Localization.getString(Strings.UI_MENU_FILE_PROJECT_SETTINGS), createFontIcon(FontAwesomeType.FILE_PEN_SOLID));
		final MenuItem menuItemSettings = new MenuItem(Localization.getString(Strings.UI_MENU_FILE_SETTINGS), createFontIcon(FontAwesomeType.GEAR_SOLID));

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_FILE));
		menu.getItems().addAll(
				menuItemNewProject,
				menuRecentProject,
				menuItemManageProject,
				menuItemSaveProject,
				new SeparatorMenuItem(),
				menuItemProjectSettings,
				new SeparatorMenuItem(),
				menuItemSettings
		);

		return menu;
	}

	private Menu createMenuEdit()
	{
		final MenuItem menuItemSearch = new MenuItem(Localization.getString(Strings.UI_MENU_EDIT_SEARCH), createFontIcon(FontAwesomeType.MAGNIFYING_GLASS_SOLID));
		final MenuItem menuItemReplaceMedia = new MenuItem(Localization.getString(Strings.UI_MENU_EDIT_REPLACE_MEDIA), createFontIcon(FontAwesomeType.FILE_AUDIO_SOLID));

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_EDIT));
		menu.getItems().addAll(
				menuItemSearch,
				new SeparatorMenuItem(),
				menuItemReplaceMedia
		);

		return menu;
	}

	private Menu createMenuView()
	{
		final MenuItem menuItemForeground = new MenuItem(Localization.getString(Strings.UI_MENU_VIEW_FOREGROUND), createFontIcon(FontAwesomeType.THUMBTACK_SOLID));
		final MenuItem menuItemFullscreen = new MenuItem(Localization.getString(Strings.UI_MENU_VIEW_FULLSCREEN), createFontIcon(FontAwesomeType.EXPAND_SOLID));
		final MenuItem menuItemTouchMode = new MenuItem(Localization.getString(Strings.UI_MENU_VIEW_TOUCH_MODE_ENABLE), createFontIcon(FontAwesomeType.HAND_POINTER_SOLID));

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_VIEW));
		menu.getItems().addAll(
				menuItemForeground,
				menuItemFullscreen,
				new SeparatorMenuItem(),
				menuItemTouchMode
		);

		return menu;
	}

	private Menu createMenuInfo()
	{
		final MenuItem menuItemAbout = new MenuItem(Localization.getString(Strings.UI_MENU_INFO_ABOUT), createFontIcon(FontAwesomeType.CIRCLE_INFO_SOLID));
		final MenuItem menuItemUpdates = new MenuItem(Localization.getString(Strings.UI_MENU_INFO_UPDATES), createFontIcon(FontAwesomeType.ARROWS_ROTATE_SOLID));

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_INFO));
		menu.getItems().addAll(
				menuItemAbout,
				new SeparatorMenuItem(),
				menuItemUpdates
		);

		return menu;
	}

	private FontIcon createFontIcon(FontIconType fontIconType)
	{
		final FontIcon icon = new FontIcon(fontIconType);
		icon.setMinWidth(20.0);
		icon.setAlignment(Pos.CENTER);

		return icon;
	}
}
