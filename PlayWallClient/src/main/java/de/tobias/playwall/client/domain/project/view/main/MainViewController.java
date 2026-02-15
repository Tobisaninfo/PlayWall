package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.thecodelabs.utils.ui.scene.SnackBar;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadMapper;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.pad.view.PadViewProvider;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageMapper;
import de.tobias.playwall.client.domain.project.*;
import de.tobias.playwall.client.domain.project.view.settings.ProjectSettingsViewController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.Size;
import de.tobias.playwall.client.view.ViewControllerBase;
import de.tobias.playwall.client.view.about.AboutDialog;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.GlobalColorPicker;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.VolumeSlider;
import de.tobias.playwall.client.view.style.ModernStyleSizeHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.tobias.playwall.client.appcontext.AppContext.Environment.GUI_TESTING;

@ViewController(path = "de/tobias/playwall/client/view/main", view = "MainView")
@RequiredArgsConstructor(onConstructor = @__({@InjectConstructor}))
@Getter(AccessLevel.PACKAGE)
public class MainViewController extends ViewControllerBase
{
	private static final int PROJECT_NAME_MAX_NUMBER_OF_CHARACTERS_IN_HEADER_BAR = 60;

	@FXML
	@SuppressWarnings({"java:S1874", "deprecation"})
	private HeaderBar headerBar;
	private Label projectTitleLabel;

	@FXML
	private PageButtons pageButtons;

	@FXML
	private StackPane rootStackPane;

	@FXML
	private GridPane padGridPane;

	@FXML
	private AnchorPane gridContainer;

	@FXML
	private HBox toolbar;
	@FXML
	private Button pageAddButton;
	@FXML
	private GlobalColorPicker globalColorPicker;
	@FXML
	private VolumeSlider volumeSlider;

	private LoadingView loadingOverlay;

	private MenuItem undoMenuItem;
	private MenuItem redoMenuItem;

	private final ErrorAlertBuilder errorAlertBuilder;

	private final FluentClient client;
	private final PadViewProvider padViewProvider;
	private final PageMapper pageMapper;
	private final PadMapper padMapper;
	private final ProjectMapper projectMapper;
	private final ProjectMetadataMapper projectMetadataMapper;
	private final UpdateMessageEventHandler eventHandler;

	private ProjectLoadedListener projectLoadedListener;
	private ProjectListener projectListener;
	private PageListener pageAddListener;
	private PadUpdateListener padUpdateListener;
	private PadLoadedListener padLoadedListener;
	private PadStatusListener padStatusListener;
	private PadPlayPositionListener padPlayPositionListener;
	private ProjectSettingsUpdateListener projectSettingsUpdateListener;
	private UndoHistoryUpdateListener undoHistoryUpdateListener;

	private SnackBar notificationPane;

	private Page currentPage;
	private final List<PadView> padViews = new ArrayList<>();

	private final ClientProjectController projectController;

	private ProjectSettingsViewController projectSettingsViewController;

	@Override
	@SuppressWarnings({"java:S1874", "deprecation"})
	protected void init()
	{
		padGridPane.getStyleClass().add("pad-grid");

		notificationPane = new SnackBar(padGridPane, new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID));
		final Action closeAction = new Action(_ -> notificationPane.hide());
		closeAction.setGraphic(new FontIcon(FontAwesomeType.XMARK_SOLID));
		notificationPane.getActions().add(closeAction);
		notificationPane.setCloseButtonVisible(false);
		gridContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);

		projectTitleLabel = new Label();
		projectTitleLabel.getStyleClass().add("window-title");
		final ImageView logoImageView = new ImageView(iconProvider.getStageIcon());
		logoImageView.setFitWidth(20);
		logoImageView.setFitHeight(20);
		final HBox headerBox = new HBox(logoImageView, projectTitleLabel, createMenu());
		headerBox.setPadding(new Insets(0, 0, 0, 14));
		headerBox.setAlignment(Pos.CENTER_LEFT);
		headerBox.setSpacing(10);
		HeaderBar.setDragType(projectTitleLabel, HeaderDragType.DRAGGABLE_SUBTREE);
		HeaderBar.setDragType(logoImageView, HeaderDragType.DRAGGABLE_SUBTREE);

		headerBar.setLeading(headerBox);

		loadingOverlay = new LoadingView();
		loadingOverlay.visibleProperty().addListener((_, _, newValue) -> pageButtons.setLoading(newValue));
		loadingOverlay.setVisible(true);
		rootStackPane.getChildren().add(loadingOverlay);

		projectLoadedListener = new ProjectLoadedListener(this);
		eventHandler.registerListener(projectLoadedListener);
		projectListener = new ProjectListener(projectMapper, this);
		eventHandler.registerListener(projectListener);
		pageAddListener = new PageListener(projectController, this, pageMapper);
		eventHandler.registerListener(pageAddListener);
		padUpdateListener = new PadUpdateListener(projectController, this, padMapper);
		eventHandler.registerListener(padUpdateListener);
		padLoadedListener = new PadLoadedListener(projectController, this);
		eventHandler.registerListener(padLoadedListener);
		padStatusListener = new PadStatusListener(projectController, this);
		eventHandler.registerListener(padStatusListener);
		padPlayPositionListener = new PadPlayPositionListener(projectController, this);
		eventHandler.registerListener(padPlayPositionListener);
		projectSettingsUpdateListener = new ProjectSettingsUpdateListener(projectController, this, projectMetadataMapper);
		eventHandler.registerListener(projectSettingsUpdateListener);
		undoHistoryUpdateListener = new UndoHistoryUpdateListener(this);
		eventHandler.registerListener(undoHistoryUpdateListener);

		globalColorPicker.init(padViews, (pad, color) -> {
			pad.setDefaultColor(color);

			try
			{
				client.pad(pad.getId()).updateSettings(pad);
			}
			catch(PlayWallApiException e)
			{
				Logger.error(e.getMessage());
				errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_COLOR_UPDATE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
			}
		});

		volumeSlider.valueProperty().addListener((_, oldValue, newValue) -> {
			if(Math.abs(oldValue.doubleValue() - newValue.doubleValue()) < VolumeSlider.UPDATE_THRESHOLD)
			{
				return;
			}

			try
			{
				client.currentProject().changeGlobalVolume(newValue.doubleValue() / 100.0);
			}
			catch(PlayWallApiException e)
			{
				Logger.error(e.getMessage());
			}
		});
		pageAddButton.setGraphic(new FontIcon(FontAwesomeType.PLUS_SOLID));
		pageAddButton.setPrefHeight(25);
		pageAddButton.setPrefWidth(25);
		pageAddButton.setMinHeight(25);
		pageAddButton.setMinWidth(25);
		pageAddButton.setMaxHeight(25);
		pageAddButton.setMaxWidth(25);

	}

	@Override
	@SuppressWarnings({"java:S1874", "deprecation"})
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stageContainer.initStyle(environment == GUI_TESTING ? StageStyle.UNDECORATED : StageStyle.EXTENDED);
		stageContainer.addCloseHook(this::closeRequest);

		stage.setOnHidden(_ -> onWindowClosed());

		stage.setTitle(getWindowTitle("-"));
		stage.show();

		projectTitleLabel.textProperty().bind(stage.titleProperty().map(t -> {
			if(t.length() > PROJECT_NAME_MAX_NUMBER_OF_CHARACTERS_IN_HEADER_BAR)
			{
				return t.substring(0, PROJECT_NAME_MAX_NUMBER_OF_CHARACTERS_IN_HEADER_BAR) + "…";
			}
			return t;
		}));
		pageButtons.prefWrapLengthProperty().bind(stage.getScene().widthProperty());

		stageContainer.addCloseKeyShortcut(() -> globalColorPicker.setSelected(false));
	}

	public void updateTitle()
	{
		getStage().setTitle(getWindowTitle(projectController.getProject().getMetadata().getName()));
	}

	private void onWindowClosed()
	{
		eventHandler.unregisterListener(projectLoadedListener);
		eventHandler.unregisterListener(projectListener);
		eventHandler.unregisterListener(pageAddListener);
		eventHandler.unregisterListener(padUpdateListener);
		eventHandler.unregisterListener(padLoadedListener);
		eventHandler.unregisterListener(padStatusListener);
		eventHandler.unregisterListener(padPlayPositionListener);
		eventHandler.unregisterListener(projectSettingsUpdateListener);
		eventHandler.unregisterListener(undoHistoryUpdateListener);

		try
		{
			client.currentProject().stopAllPads();
		}
		catch(PlayWallApiException e)
		{
			throw new RuntimeException(e);
		}
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
		final Size minSize = computeMinStageSize(project.getMetadata().getNumberOfHorizontalPads(), project.getMetadata().getNumberOfVerticalPads());

		stage.setMinWidth(minSize.width());
		stage.setMinHeight(minSize.height());

		stage.setTitle(getWindowTitle(project.getMetadata().getName()));
	}

	private Stage getStage()
	{
		return getStageContainer().map(NVCStage::getStage).orElseThrow();
	}

	// Project handling

	public void showProject(Project project)
	{
		try
		{
			loadingOverlay.resetAndSetPadCount((int) project.getPadCountWithContent());
			if(!loadingOverlay.isVisible())
			{
				loadingOverlay.show();
			}
			this.projectController.loadProject(project);

			updateWindowProperties(project);
			initializePadViews(project.getMetadata().getNumberOfHorizontalPads(), project.getMetadata().getNumberOfVerticalPads());


			buildPageButtons();
			showPage(0);

			client.project(project.getMetadata().getId()).load();

			volumeSlider.setValue(projectController.getProject().getMetadata().getVolume() * 100);
			Logger.info("Loading project " + project.getMetadata().getName());
		}
		catch(PlayWallApiException e)
		{
			loadingOverlay.hide();
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_LOAD), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	public void updateProject(Project project)
	{
		this.projectController.updateProject(project);

		Platform.runLater(() -> {

			updateWindowProperties(project);
			initializePadViews(project.getMetadata().getNumberOfHorizontalPads(), project.getMetadata().getNumberOfVerticalPads());

			buildPageButtons();
			showPage(currentPage.getPosition());

			Logger.info("Update project " + project.getMetadata().getName());
		});
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
		padViews.forEach(view -> padGridPane.getChildren().remove(view.getRootNode()));
		padViews.clear();
	}

	void buildPageButtons()
	{
		pageButtons.buildPageButtons(projectController.getProject().getPages(), (button, page) -> {
			button.setOnAction(_ -> showPage(page));
			button.setContextMenu(new ContextMenu(
					createMenuItem(Strings.UI_PAGE_RENAME, FontAwesomeType.PENCIL_SOLID, Optional.of(_ -> onPageRenameMenuItem(page))),
					createMenuItem(Strings.UI_PAGE_DUPLICATE, FontAwesomeType.COPY_SOLID, Optional.of(_ -> onPageDuplicateMenuItem(page))),
					createMenuItem(Strings.UI_PAGE_DELETE, FontAwesomeType.TRASH_SOLID, Optional.of(_ -> onPageDeleteMenuItem(page)))
			));
		});

		pageButtons.highlightPageButton(currentPage);
	}

	private void onPageRenameMenuItem(Page page)
	{
		final TextInputDialog dialog = new TextInputDialog(page.getName());
		dialog.setTitle(Localization.getString(Strings.UI_PAGE_RENAME_TITLE));
		dialog.setHeaderText(Localization.getString(Strings.UI_PAGE_RENAME_TITLE));
		dialog.setContentText(Localization.getString(Strings.UI_PAGE_RENAME_INPUT));
		dialog.getDialogPane().setMinWidth(400);
		dialog.initOwner(getStage());

		final TextField textField = dialog.getEditor();

		final Label errorLabel = new Label(Localization.getString(Strings.UI_PAGE_RENAME_ERROR_EMPTY));
		errorLabel.getStyleClass().add("error-label");
		errorLabel.setVisible(false);
		errorLabel.setPadding(new Insets(10, 0, 0, 0));

		final GridPane content = (GridPane) dialog.getDialogPane().getContent();
		content.add(errorLabel, 1, 1);

		final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
		okButton.addEventFilter(ActionEvent.ACTION, event -> {
			final String newName = textField.getText().trim();

			if(newName.isEmpty())
			{
				errorLabel.setText(Localization.getString(Strings.UI_PAGE_RENAME_ERROR_EMPTY));
				errorLabel.setVisible(true);
				event.consume();
				return;
			}

			final List<String> usedPageNames = projectController.getProject().getPages().stream()
					.map(Page::getName)
					.toList();

			if(usedPageNames.contains(newName))
			{
				errorLabel.setText(Localization.getString(Strings.UI_PAGE_RENAME_ERROR_DUPLICATE));
				errorLabel.setVisible(true);
				event.consume();
			}
		});

		final Optional<String> result = dialog.showAndWait();
		result.ifPresent(newPageName -> {
			if(!newPageName.trim().isEmpty())
			{
				try
				{
					client.currentProject().page(page.getId()).rename(newPageName.trim());
				}
				catch(PlayWallApiException e)
				{
					Logger.error(e);
					showErrorMessage(e.getMessage());
				}
			}
		});
	}

	private void onPageDuplicateMenuItem(Page page)
	{
		try
		{
			client.currentProject().page(page.getId()).duplicate();
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_DUPLICATE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onPageDeleteMenuItem(Page page)
	{
		try
		{
			client.currentProject().page(page.getId()).delete();
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_DELETE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	private void onPageReorder(PageButtons.PageReorderEvent event)
	{
		try
		{
			client.currentProject().reorderPages(event.getPages().stream().collect(Collectors.toMap(Page::getId, page -> event.getPages().indexOf(page))));
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_REORDER), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	public void showLoadingOverlay(boolean visible)
	{
		loadingOverlay.setVisible(visible);
	}

	public void showPage(int position)
	{
		final Page page = projectController.getProject().getPage(position);
		showPage(page);
	}

	public void showPage(Page page)
	{
		this.currentPage = page;
		final ProjectMetadata projectMetadata = projectController.getProject().getMetadata();
		final int padNumberPerPage = projectMetadata.getNumberOfPadsPerPage();

		for(int i = 0; i < padNumberPerPage; i++)
		{
			final PadView view = padViews.get(i);
			final Pad pad = page.getPad(i);

			view.updateFromPad(page.getPosition(), projectController.getPadController(pad.getId()));
		}

		// Highlight the current page button
		pageButtons.highlightPageButton(page);

		styleable.renderStylesheets(getStage(), page, projectMetadata);
	}

	public void updateStyle()
	{
		styleable.renderStylesheets(getStage(), getCurrentPage(), projectController.getProject().getMetadata());
	}

	public PadView getPadViewForPosition(int position)
	{
		return padViews.get(position);
	}

	public PadView getPadViewForPadId(UUID padId)
	{
		return padViews.stream()
				.filter(view -> view.getPadController().getPad().getId().equals(padId))
				.findFirst().orElse(null);
	}

	public void showNotification(String message)
	{
		notificationPane.showAndHide(message, ViewConstants.DEFAULT_SNACKBAR_SHOW);
	}

	// Action Handlers

	@FXML
	private void onPageAdd(ActionEvent event)
	{
		try
		{
			client.currentProject().addPage();
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_ADD), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	// Menu

	private Node createMenu()
	{
		final Menu menuFile = createMenuFile();
		final Menu menuEdit = createMenuEdit();
		final Menu menuView = createMenuView();
		final Menu menuInfo = createMenuInfo();

		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuInfo);
		return menuBar;
	}

	private Menu createMenuFile()
	{
		final MenuItem menuItemNewProject = createMenuItem(Strings.UI_MENU_FILE_NEW_PROJECT, FontAwesomeType.FOLDER_PLUS_SOLID, Optional.empty());
		final Menu menuRecentProject = new Menu(Localization.getString(Strings.UI_MENU_FILE_RECENT_PROJECT), createFontIcon(FontAwesomeType.CLOCK_ROTATE_LEFT_SOLID));
		final MenuItem menuItemManageProject = createMenuItem(Strings.UI_MENU_FILE_MANAGE_PROJECTS, FontAwesomeType.FOLDER_TREE_SOLID, Optional.empty());
		final MenuItem menuItemSaveProject = createMenuItem(Strings.UI_MENU_FILE_SAVE_PROJECT, FontAwesomeType.FLOPPY_DISK_SOLID, Optional.of(this::onMenuItemSave), new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN));
		final MenuItem menuItemProjectSettings = createMenuItem(Strings.UI_MENU_FILE_PROJECT_SETTINGS, FontAwesomeType.FILE_PEN_SOLID, Optional.of(this::onMenuItemProjectSettings), new KeyCharacterCombination(",", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		final MenuItem menuItemSettings = createMenuItem(Strings.UI_MENU_FILE_SETTINGS, FontAwesomeType.GEAR_SOLID, Optional.empty());

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
		undoMenuItem = createMenuItem(Strings.UI_MENU_EDIT_UNDO, FontAwesomeType.ROTATE_LEFT_SOLID, Optional.of(this::onMenuItemUndo), new KeyCharacterCombination("Z", KeyCombination.SHORTCUT_DOWN));
		undoMenuItem.setDisable(true);
		redoMenuItem = createMenuItem(Strings.UI_MENU_EDIT_REDO, FontAwesomeType.ROTATE_RIGHT_SOLID, Optional.of(this::onMenuItemRedo), new KeyCharacterCombination("Z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		redoMenuItem.setDisable(true);
		final MenuItem menuItemSearch = createMenuItem(Strings.UI_MENU_EDIT_SEARCH, FontAwesomeType.MAGNIFYING_GLASS_SOLID, Optional.empty());
		final MenuItem menuItemReplaceMedia = createMenuItem(Strings.UI_MENU_EDIT_REPLACE_MEDIA, FontAwesomeType.FILE_AUDIO_SOLID, Optional.empty());

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_EDIT));
		menu.getItems().addAll(
				undoMenuItem,
				redoMenuItem,
				menuItemSearch,
				new SeparatorMenuItem(),
				menuItemReplaceMedia
		);

		return menu;
	}

	private Menu createMenuView()
	{
		final MenuItem menuItemForeground = createMenuItem(Strings.UI_MENU_VIEW_FOREGROUND, FontAwesomeType.THUMBTACK_SOLID, Optional.empty());
		final MenuItem menuItemFullscreen = createMenuItem(Strings.UI_MENU_VIEW_FULLSCREEN, FontAwesomeType.EXPAND_SOLID, Optional.empty());
		final MenuItem menuItemTouchMode = createMenuItem(Strings.UI_MENU_VIEW_TOUCH_MODE_ENABLE, FontAwesomeType.HAND_POINTER_SOLID, Optional.empty());

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
		final MenuItem menuItemAbout = createMenuItem(Strings.UI_MENU_INFO_ABOUT, FontAwesomeType.CIRCLE_INFO_SOLID, Optional.of(this::onMenuItemAbout));
		final MenuItem menuItemUpdates = createMenuItem(Strings.UI_MENU_INFO_UPDATES, FontAwesomeType.ARROWS_ROTATE_SOLID, Optional.empty());

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_INFO));
		menu.getItems().addAll(
				menuItemAbout,
				new SeparatorMenuItem(),
				menuItemUpdates
		);

		return menu;
	}

	private MenuItem createMenuItem(String localizationKey, FontIconType fontIconType, Optional<EventHandler<ActionEvent>> eventHandler)
	{
		return createMenuItem(localizationKey, fontIconType, eventHandler, KeyCombination.NO_MATCH);
	}

	private MenuItem createMenuItem(String localizationKey, FontIconType fontIconType, Optional<EventHandler<ActionEvent>> eventHandler, KeyCombination shortcut)
	{
		final MenuItem menuItem = new MenuItem(Localization.getString(localizationKey), createFontIcon(fontIconType));
		menuItem.setAccelerator(shortcut);
		eventHandler.ifPresent(handler -> menuItem.setOnAction(event -> {
			if(loadingOverlay.isVisible())
			{
				return;
			}
			handler.handle(event);
		}));
		menuItem.setDisable(eventHandler.isEmpty());
		return menuItem;
	}

	private FontIcon createFontIcon(FontIconType fontIconType)
	{
		final FontIcon icon = new FontIcon(fontIconType);
		icon.setMinWidth(20.0);
		icon.setAlignment(Pos.CENTER);

		return icon;
	}

	private void onMenuItemSave(ActionEvent event)
	{
		try
		{
			client.currentProject().save();
			notificationPane.showAndHide(Localization.getString("ui.notification.project.saved"), ViewConstants.DEFAULT_SNACKBAR_SHOW);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onMenuItemUndo(ActionEvent event)
	{
		try
		{
			undoMenuItem.setDisable(true);
			client.currentProject().undo();
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_UNDO), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onMenuItemRedo(ActionEvent event)
	{
		try
		{
			redoMenuItem.setDisable(true);
			client.currentProject().redo();
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_REDO), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onMenuItemAbout(ActionEvent event)
	{
		final AboutDialog aboutDialog = AppContextHolder.getInstance().get(AboutDialog.class);
		aboutDialog.showAndWait(getContainingWindow());
	}

	private void onMenuItemProjectSettings(ActionEvent event)
	{
		if(projectSettingsViewController == null)
		{
			projectSettingsViewController = AppContextHolder.getInstance().get(ProjectSettingsViewController.class);
		}

		projectSettingsViewController.showAndWait(new ProjectSettingsViewController.Param(projectController.getProject().getMetadata()), getContainingWindow());
	}
}
