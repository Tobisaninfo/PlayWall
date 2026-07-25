package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.pad.*;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.pad.view.PadViewProvider;
import de.tobias.playwall.client.domain.pad.view.desktop.PadEventDispatcher;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.FileDragListener;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.GlobalPickerColorListener;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.PadDragListener;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageMapper;
import de.tobias.playwall.client.domain.page.PageSettingsMapper;
import de.tobias.playwall.client.domain.page.view.settings.BasePageSettingsViewController;
import de.tobias.playwall.client.domain.page.view.settings.PageSettingsViewController;
import de.tobias.playwall.client.domain.page.view.PageButtons;
import de.tobias.playwall.client.domain.page.view.PageButtonsEventDispatcher;
import de.tobias.playwall.client.domain.page.view.PageButtonInputListener;
import de.tobias.playwall.client.domain.project.*;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.management.ProjectManagementViewController;
import de.tobias.playwall.client.domain.project.view.media.MissingMediaEntry;
import de.tobias.playwall.client.domain.project.view.media.ReplaceMediaViewController;
import de.tobias.playwall.client.domain.project.view.settings.BaseProjectSettingsViewController;
import de.tobias.playwall.client.domain.project.view.settings.ProjectSettingsViewController;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.SettingsMapper;
import de.tobias.playwall.client.domain.settings.view.main.SettingsListener;
import de.tobias.playwall.client.domain.settings.view.settings.BaseProgramSettingsViewController;
import de.tobias.playwall.client.domain.settings.view.settings.ProgramSettingsViewController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.log.LogViewer;
import de.tobias.playwall.client.net.ConnectionState;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.ExportFile;
import de.tobias.playwall.client.utils.MimeType;
import de.tobias.playwall.client.utils.Size;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.ViewControllerBase;
import de.tobias.playwall.client.view.about.AboutDialog;
import de.tobias.playwall.client.view.components.*;
import de.tobias.playwall.client.view.style.ModernStyleSizeHelper;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.client.view.toast.MaterialToastManager;
import de.tobias.playwall.client.view.toast.ToastType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static de.thecodelabs.utils.util.Localization.getString;
import static de.tobias.playwall.client.appcontext.AppContext.Environment.GUI_TESTING;
import static de.tobias.playwall.client.view.components.ViewConstants.DEFAULT_CONTEXT_MANU_GAP;

@ViewController(path = "de/tobias/playwall/client/view/main", view = "MainView")
@RequiredArgsConstructor(onConstructor = @__({@InjectConstructor}))
@Getter(AccessLevel.PACKAGE)
@Slf4j
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
	private ContextMenu pageAddButtonContextMenu;
	@FXML
	private GlobalColorPicker globalColorPicker;
	@FXML
	private VolumeSlider volumeSlider;

	private LoadingView loadingOverlay;
	private ConnectionLostOverlay connectionLostOverlay;

	private Label connectionStatusLabel;
	private final AtomicBoolean isReconnecting = new AtomicBoolean(false);

	private MenuItem undoMenuItem;
	private MenuItem redoMenuItem;

	@Getter(AccessLevel.PROTECTED)
	private Menu menuRecentProjects;

	private final ErrorAlertBuilder errorAlertBuilder;

	private final FluentClient client;
	private final FileChooserWrapper fileChooserWrapper;
	private final PadViewProvider padViewProvider;
	private final PageMapper pageMapper;
	private final PageSettingsMapper pageSettingsMapper;
	private final PadMapper padMapper;
	private final ProjectMapper projectMapper;
	private final ProjectMetadataMapper projectMetadataMapper;
	private final SettingsMapper settingsMapper;

	private final UpdateMessageEventHandler eventHandler;
	private final PageButtonsEventDispatcher pageEventDispatcher;
	private final PadEventDispatcher padEventDispatcher;

	private ProjectLoadedListener projectLoadedListener;
	private ProjectListener projectListener;
	private PageListener pageAddListener;
	private PadUpdateListener padUpdateListener;
	private PadLoadedListener padLoadedListener;
	private PadStatusListener padStatusListener;
	private PadReplaceListener padReplaceListener;
	private PadSwapListener padSwapListener;
	private PadPlayPositionListener padPlayPositionListener;
	private ProjectSettingsUpdateListener projectSettingsUpdateListener;
	private UndoHistoryUpdateListener undoHistoryUpdateListener;
	private SettingsListener settingsListener;

	private MaterialToastManager materialToastManager;

	private Page currentPage;
	private final List<PadView> padViews = new ArrayList<>();

	private final ClientProjectController projectController;
	private final ClientSettingsController settingsController;

	private ProjectSettingsViewController projectSettingsViewController;
	private ProgramSettingsViewController programSettingsViewController;

	@Override
	@SuppressWarnings({"java:S1874", "deprecation"})
	protected void init()
	{
		padGridPane.getStyleClass().add("pad-grid");

		materialToastManager = new MaterialToastManager(rootStackPane);

		projectTitleLabel = new Label();
		projectTitleLabel.getStyleClass().add("window-title");
		final ImageView logoImageView = new ImageView(iconProvider.getStageIcon());
		logoImageView.setFitWidth(20);
		logoImageView.setFitHeight(20);
		final HBox headerBox = new HBox(logoImageView, projectTitleLabel, createMenu());
		headerBox.setPadding(new Insets(0, 0, 0, ViewConstants.DEFAULT_SPACING / 2));
		headerBox.setAlignment(Pos.CENTER_LEFT);
		headerBox.setSpacing(10);
		HeaderBar.setDragType(projectTitleLabel, HeaderDragType.DRAGGABLE_SUBTREE);
		HeaderBar.setDragType(logoImageView, HeaderDragType.DRAGGABLE_SUBTREE);

		headerBar.setLeading(headerBox);

		connectionStatusLabel = new Label();
		connectionStatusLabel.getStyleClass().add("connection-status");
		headerBar.setTrailing(connectionStatusLabel);

		loadingOverlay = new LoadingView();
		loadingOverlay.visibleProperty().addListener((_, _, newValue) -> pageButtons.setLoading(newValue));
		loadingOverlay.setVisible(true);
		rootStackPane.getChildren().add(loadingOverlay);

		connectionLostOverlay = new ConnectionLostOverlay(Platform::exit, app);
		rootStackPane.getChildren().add(connectionLostOverlay);

		if(client.connectionStateProperty() != null)
		{
			client.connectionStateProperty().addListener((_, _, newState) ->
					Platform.runLater(() -> onConnectionStateChanged(newState)));
			onConnectionStateChanged(client.connectionStateProperty().get());
		}

		projectLoadedListener = new ProjectLoadedListener(this, projectController, settingsController);
		eventHandler.registerListener(projectLoadedListener);
		projectListener = new ProjectListener(projectMapper, this);
		eventHandler.registerListener(projectListener);
		pageAddListener = new PageListener(projectController, this, pageMapper, pageSettingsMapper);
		eventHandler.registerListener(pageAddListener);
		padUpdateListener = new PadUpdateListener(projectController, this, padMapper);
		eventHandler.registerListener(padUpdateListener);
		padLoadedListener = new PadLoadedListener(projectController, this);
		eventHandler.registerListener(padLoadedListener);
		padStatusListener = new PadStatusListener(projectController, this);
		eventHandler.registerListener(padStatusListener);
		padReplaceListener = new PadReplaceListener(projectController, padMapper, this);
		eventHandler.registerListener(padReplaceListener);
		padSwapListener = new PadSwapListener(projectController, this);
		eventHandler.registerListener(padSwapListener);
		padPlayPositionListener = new PadPlayPositionListener(projectController, this);
		eventHandler.registerListener(padPlayPositionListener);
		projectSettingsUpdateListener = new ProjectSettingsUpdateListener(projectController, this, projectMetadataMapper);
		eventHandler.registerListener(projectSettingsUpdateListener);
		undoHistoryUpdateListener = new UndoHistoryUpdateListener(this);
		eventHandler.registerListener(undoHistoryUpdateListener);
		settingsListener = new SettingsListener(settingsMapper, settingsController);
		eventHandler.registerListener(settingsListener);

		globalColorPicker.init(padEventDispatcher, padGridPane, new GlobalPickerColorListener(globalColorPicker, this::onColorChange, this::onColorSubmit));

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
				log.error("Cannot update global volume", e);
			}
		});
		pageAddButton.setGraphic(new FontIcon(FontAwesomeType.PLUS_SOLID));
		pageAddButton.setPrefSize(30, 30);
		pageAddButton.setMinSize(30, 30);
		pageAddButton.setMaxSize(30, 30);

		pageAddButtonContextMenu = new ContextMenu();
		final MenuItem newPageMenuItem = new MenuItem(getString(Strings.UI_PAGE_ADD_NEW), new FontIcon(FontAwesomeType.PLUS_SOLID));
		newPageMenuItem.setOnAction(this::onPageAddNew);
		final MenuItem importPageMenuItem = new MenuItem(getString(Strings.UI_PAGE_ADD_IMPORT), new FontIcon(FontAwesomeType.FILE_IMPORT_SOLID));
		importPageMenuItem.setOnAction(this::onPageImport);
		pageAddButtonContextMenu.getItems().addAll(newPageMenuItem, importPageMenuItem);

		padEventDispatcher.addPadInputListener(new FileDragListener());
		final PadDragListener padDragListener = new PadDragListener(client, projectController, errorAlertBuilder, this);
		padEventDispatcher.addPadInputListener(padDragListener);

		pageEventDispatcher.addPageInputListener(new PageButtonInputListener()
		{
			@Override
			public void onAction(Page page, ActionEvent event)
			{
				showPage(page);
			}
		});
		pageEventDispatcher.addPageInputListener(padDragListener);
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
		eventHandler.unregisterListener(settingsListener);

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
		if(projectController.isAtLeastOnePadPlaying())
		{
			showNotification(Localization.getString(Strings.UI_EXIT_WARNING_PLAYING), ToastType.WARNING);
			return false;
		}

		try
		{
			final boolean isSaved = client.currentProject().isSaved();
			if(isSaved)
			{
				return true;
			}

			switch(settingsController.getSettings().getUnsavedChangesMode())
			{
				case ASK ->
				{
					final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
					alert.setTitle(getString(Strings.UI_DIALOG_EXIT_UNSAVED_CHANGES_TITLE));
					alert.setContentText(getString(Strings.UI_DIALOG_EXIT_UNSAVED_CHANGES_CONTENT));
					alert.initOwner(getContainingWindow());
					if(AppContextHolder.getInstance().getEnvironment() != AppContext.Environment.GUI_TESTING)
					{
						alert.initModality(Modality.WINDOW_MODAL);
						alert.getDialogPane().setMinHeight(Double.NEGATIVE_INFINITY);
						final Optional<ButtonType> selectedButtonOptional = alert.showAndWait();
						return selectedButtonOptional.isPresent() && selectedButtonOptional.get() == ButtonType.OK;
					}
					else
					{
						return false;
					}
				}
				case SAVE ->
				{
					client.currentProject().save();
					return true;
				}
				case DISCARD ->
				{
					return true;
				}
			}
		}
		catch(PlayWallApiException e)
		{
			log.error("Error fetching project save status", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_SAVE_STATUS), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
		catch(RuntimeException e)
		{
			log.error("Error fetching project save status", e);
			return true;
		}

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
			if(projectController.getProject() != null)
			{
				client.currentProject().close();
			}

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
			log.info("Loading project {}", project.getMetadata().getName());
		}
		catch(PlayWallApiException e)
		{
			loadingOverlay.hide();
			log.error("Cannot show project", e);
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

			log.info("Update project {}", project.getMetadata().getName());
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
		pageButtons.buildPageButtons(projectController.getProject().getPages(), page -> {
			final MenuItem deleteMenuItem = createMenuItem(Strings.UI_PAGE_DELETE, FontAwesomeType.TRASH_CAN_SOLID, Optional.of(_ -> onPageDeleteMenuItem(page)));
			deleteMenuItem.getStyleClass().add("danger");
			return new ContextMenu(
					createMenuItem(Strings.UI_PAGE_SETTINGS, FontAwesomeType.GEAR_SOLID, Optional.of(_ -> onPageSettingsMenuItem(page))),
					createMenuItem(Strings.UI_PAGE_DUPLICATE, FontAwesomeType.COPY_SOLID, Optional.of(_ -> onPageDuplicateMenuItem(page))),
					createMenuItem(Strings.UI_PAGE_EXPORT, FontAwesomeType.FILE_IMPORT_SOLID, Optional.of(_ -> onPageExportMenuItem(page))),
					new SeparatorMenuItem(),
					deleteMenuItem
			);
		});

		pageButtons.highlightPageButton(currentPage);
	}

	private void onPageSettingsMenuItem(Page page)
	{
		final PageSettingsViewController controller = AppContextHolder.getInstance().get(PageSettingsViewController.class);
		controller.showAndWait(new BasePageSettingsViewController.Param(page), getContainingWindow());
	}

	private void onPageDuplicateMenuItem(Page page)
	{
		try
		{
			client.currentProject().page(page.getId()).duplicate();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot duplicate page", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_DUPLICATE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onPageExportMenuItem(Page page)
	{
		try
		{
			final ExportFile export = client.currentProject().page(page.getId()).export();
			final String initialFileName = Localization.getString(Strings.UI_PAGE_EXPORT_NAME,
							projectController.getProject().getMetadata().getName(),
							page.getSettings().getName())
					.replaceAll("[^a-zA-Z0-9\\s\\-_]", "_");

			final MimeType mimeType = MimeType.getByMimeType(export.mimetype());
			fileChooserWrapper.setExtensionFilter(List.of(mimeType.toExtensionFilter()));
			fileChooserWrapper.setInitialFilename(initialFileName + "." + mimeType.getExtension());
			final Optional<Path> pathOptional = fileChooserWrapper.showSaveFile(getContainingWindow());
			if(pathOptional.isEmpty())
			{
				return;
			}
			final Path path = pathOptional.get();
			Files.write(path, export.data());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot export page", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_EXPORT), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
		catch(IOException e)
		{
			log.error("Cannot write file", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_EXPORT), e.getMessage(), getContainingWindow()).showAndWait();
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
			log.error("Cannot delete page", e);
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
			log.error("Cannot reorder page", e);
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

	public void showNotification(String message, ToastType toastType)
	{
		if(!Platform.isFxApplicationThread())
		{
			Platform.runLater(() -> showNotification(message, toastType));
			return;
		}
		final String title = switch(toastType)
		{
			case SUCCESS -> Localization.getString(Strings.UI_NOTIFICATION_SUCCESS);
			case INFO -> Localization.getString(Strings.UI_NOTIFICATION_INFO);
			case WARNING -> Localization.getString(Strings.UI_NOTIFICATION_WARNING);
			case ERROR -> Localization.getString(Strings.UI_NOTIFICATION_ERROR);
		};
		materialToastManager.show(title, message, toastType);
	}

	private void onConnectionStateChanged(ConnectionState state)
	{
		connectionStatusLabel.getPseudoClassStates().forEach(pseudoClass -> connectionStatusLabel.pseudoClassStateChanged(pseudoClass, false));
		switch(state)
		{
			case CONNECTED ->
			{
				connectionStatusLabel.setText("● " + Localization.getString(Strings.UI_CONNECTION_STATE_CONNECTED));
				connectionStatusLabel.pseudoClassStateChanged(PseudoClasses.SUCCESS_CLASS, true);
				connectionLostOverlay.setVisible(false);
			}
			case RECONNECTING ->
			{
				connectionStatusLabel.setText("● " + Localization.getString(Strings.UI_CONNECTION_STATE_RECONNECTING));
				connectionStatusLabel.pseudoClassStateChanged(PseudoClasses.WARNING_CLASS, true);
			}
			case DISCONNECTED ->
			{
				connectionStatusLabel.setText("● " + Localization.getString(Strings.UI_CONNECTION_STATE_DISCONNECTED));
				connectionStatusLabel.pseudoClassStateChanged(PseudoClasses.DANGER_CLASS, true);
				if(isReconnecting.compareAndSet(false, true))
				{
					startReconnecting();
				}
			}
		}
	}

	private void startReconnecting()
	{
		final int maxRetries = 5;
		Worker.runLater(() -> {
			try
			{
				client.connectWithRetries(maxRetries, (currentTry, max) ->
						log.info("Reconnect attempt {}/{} failed", currentTry, max));
			}
			catch(Exception e)
			{
				log.error("Reconnect failed after {} attempts", maxRetries, e);
				Platform.runLater(() -> {
					connectionStatusLabel.setText("● " + Localization.getString(Strings.UI_CONNECTION_STATE_DISCONNECTED));
					connectionStatusLabel.pseudoClassStateChanged(PseudoClasses.DANGER_CLASS, true);
					connectionLostOverlay.setVisible(true);
				});
			}
			finally
			{
				isReconnecting.set(false);
			}
		});
	}

	// Action Handlers

	@FXML
	private void onPageAdd(ActionEvent event)
	{
		pageAddButtonContextMenu.show(pageAddButton, Side.BOTTOM, 0, DEFAULT_CONTEXT_MANU_GAP);
	}

	private void onPageAddNew(ActionEvent event)
	{
		try
		{
			client.currentProject().addPage();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot add page", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_ADD), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onPageImport(ActionEvent event)
	{
		final MimeType mimeType = MimeType.APPLICATION_JSON;
		fileChooserWrapper.setExtensionFilter(List.of(mimeType.toExtensionFilter()));
		final Optional<Path> pathOptional = fileChooserWrapper.showOpenFile(getContainingWindow());
		if(pathOptional.isEmpty())
		{
			return;
		}
		try
		{
			final byte[] bytes = Files.readAllBytes(pathOptional.get());
			client.currentProject().importPage(new ExportFile(mimeType.getMimeTypeValue(), bytes));
		}
		catch(IOException e)
		{
			log.error("Cannot read file", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_IMPORT), e.getMessage(), getContainingWindow()).showAndWait();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot import page", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_IMPORT), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
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
		final MenuItem menuItemNewProject = createMenuItem(Strings.UI_MENU_FILE_NEW_PROJECT, FontAwesomeType.FOLDER_PLUS_SOLID, Optional.of(this::onMenuItemNewProject), new KeyCharacterCombination("N", KeyCombination.SHORTCUT_DOWN));
		menuRecentProjects = new Menu(Localization.getString(Strings.UI_MENU_FILE_RECENT_PROJECT), createFontIcon(FontAwesomeType.CLOCK_ROTATE_LEFT_SOLID));
		final MenuItem menuItemManageProjects = createMenuItem(Strings.UI_MENU_FILE_MANAGE_PROJECTS, FontAwesomeType.FOLDER_TREE_SOLID, Optional.of(this::onMenuItemManageProjects), new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN));
		final MenuItem menuItemSaveProject = createMenuItem(Strings.UI_MENU_FILE_SAVE_PROJECT, FontAwesomeType.FLOPPY_DISK_SOLID, Optional.of(this::onMenuItemSave), new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN));
		final MenuItem menuItemProjectSettings = createMenuItem(Strings.UI_MENU_FILE_PROJECT_SETTINGS, FontAwesomeType.FILE_PEN_SOLID, Optional.of(this::onMenuItemProjectSettings), new KeyCharacterCombination(",", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		final MenuItem menuItemSettings = createMenuItem(Strings.UI_MENU_FILE_SETTINGS, FontAwesomeType.GEAR_SOLID, Optional.of(this::onMenuItemSettings), new KeyCharacterCombination(",", KeyCombination.SHORTCUT_DOWN));

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_FILE));
		menu.getItems().addAll(
				menuItemNewProject,
				menuRecentProjects,
				menuItemManageProjects,
				menuItemSaveProject,
				new SeparatorMenuItem(),
				menuItemProjectSettings,
				new SeparatorMenuItem(),
				menuItemSettings
		);

		return menu;
	}

	public void updateMenuRecentProjects(AllProjectsInfo allProjectsInfo)
	{
		menuRecentProjects.getItems().setAll(
				allProjectsInfo.getRecentProjectIds().stream()
						.filter(id -> !id.equals(projectController.getProject().getMetadata().getId()))
						.map(id -> allProjectsInfo.getAllProjectsMetadata().stream()
								.filter(m -> m.getId().equals(id))
								.findFirst()
						)
						.filter(Optional::isPresent)
						.map(metadataOptional -> {
							final ProjectMetadata metadata = metadataOptional.get();
							final MenuItem menuItem = new MenuItem(metadata.getName());
							menuItem.setOnAction(_ -> closeCurrentProjectAndOpenProject(metadata.getId()));
							return menuItem;
						})
						.toList()
		);
	}

	public void closeCurrentProjectAndOpenProject(UUID recentProjectId)
	{
		if(!closeRequest())
		{
			return;
		}

		try
		{
			final Project project = client.project(recentProjectId).get();
			showProject(project);
			updateMenuRecentProjects(client.projects().list());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot open recent project", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_LOAD), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private Menu createMenuEdit()
	{
		undoMenuItem = createMenuItem(Strings.UI_MENU_EDIT_UNDO, FontAwesomeType.ROTATE_LEFT_SOLID, Optional.of(this::onMenuItemUndo), new KeyCharacterCombination("Z", KeyCombination.SHORTCUT_DOWN));
		undoMenuItem.setDisable(true);
		redoMenuItem = createMenuItem(Strings.UI_MENU_EDIT_REDO, FontAwesomeType.ROTATE_RIGHT_SOLID, Optional.of(this::onMenuItemRedo), new KeyCharacterCombination("Z", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		redoMenuItem.setDisable(true);
		final MenuItem menuItemSearch = createMenuItem(Strings.UI_MENU_EDIT_SEARCH, FontAwesomeType.MAGNIFYING_GLASS_SOLID, Optional.empty());
		final MenuItem menuItemReplaceMedia = createMenuItem(Strings.UI_MENU_EDIT_REPLACE_MEDIA, FontAwesomeType.FILE_AUDIO_SOLID, Optional.of(this::onMenuItemReplaceMedia));

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
		final MenuItem menuItemLog = createMenuItem(Strings.UI_MENU_INFO_LOG, FontAwesomeType.INBOX_SOLID, Optional.of(this::onMenuItemLog), true);
		final MenuItem menuItemUpdates = createMenuItem(Strings.UI_MENU_INFO_UPDATES, FontAwesomeType.ARROWS_ROTATE_SOLID, Optional.empty());

		final Menu menu = new Menu(Localization.getString(Strings.UI_MENU_INFO));
		menu.getItems().addAll(
				menuItemAbout,
				new SeparatorMenuItem(),
				menuItemLog,
				menuItemUpdates
		);

		return menu;
	}

	private MenuItem createMenuItem(String localizationKey, FontIconType fontIconType, Optional<EventHandler<ActionEvent>> eventHandler)
	{
		return createMenuItem(localizationKey, fontIconType, eventHandler, false);
	}

	private MenuItem createMenuItem(String localizationKey, FontIconType fontIconType, Optional<EventHandler<ActionEvent>> eventHandler, boolean overrideOverlayPolicy)
	{
		return createMenuItem(localizationKey, fontIconType, eventHandler, KeyCombination.NO_MATCH, overrideOverlayPolicy);
	}

	private MenuItem createMenuItem(String localizationKey, FontIconType fontIconType, Optional<EventHandler<ActionEvent>> eventHandler, KeyCombination shortcut)
	{
		return createMenuItem(localizationKey, fontIconType, eventHandler, shortcut, false);
	}

	private MenuItem createMenuItem(String localizationKey, FontIconType fontIconType, Optional<EventHandler<ActionEvent>> eventHandler, KeyCombination shortcut, boolean overrideOverlayPolicy)
	{
		final MenuItem menuItem = new MenuItem(Localization.getString(localizationKey), createFontIcon(fontIconType));
		menuItem.setAccelerator(shortcut);
		eventHandler.ifPresent(handler -> menuItem.setOnAction(event -> {
			if(isAnyOverlayVisible() && !overrideOverlayPolicy)
			{
				return;
			}
			handler.handle(event);
		}));
		menuItem.setDisable(eventHandler.isEmpty());
		return menuItem;
	}

	private boolean isAnyOverlayVisible()
	{
		return rootStackPane.getChildren()
				.stream()
				.filter(node -> node.getStyleClass().contains("overlay"))
				.anyMatch(Node::isVisible);
	}

	private FontIcon createFontIcon(FontIconType fontIconType)
	{
		final FontIcon icon = new FontIcon(fontIconType);
		icon.setMinWidth(20.0);
		icon.setAlignment(Pos.CENTER);

		return icon;
	}

	private void onMenuItemNewProject(ActionEvent event)
	{
		final ProjectNewDialog dialog = AppContextHolder.getInstance().get(ProjectNewDialog.class);
		final Optional<ProjectMetadata> projectOptional = dialog.showAndWait(getContainingWindow());
		projectOptional.ifPresent(projectMetadata -> closeCurrentProjectAndOpenProject(projectMetadata.getId()));
	}

	private void onMenuItemManageProjects(ActionEvent event)
	{
		final ProjectManagementViewController controller = AppContextHolder.getInstance().get(ProjectManagementViewController.class);
		controller.showAndWait(new ProjectManagementViewController.Param(this), getContainingWindow());
	}

	private void onMenuItemSave(ActionEvent event)
	{
		try
		{
			client.currentProject().save();
			showNotification(Localization.getString("ui.notification.project.saved"), ToastType.SUCCESS);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot save project", e);
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
			log.error("Cannot perform undo", e);
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
			log.error("Cannot perform redo", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_REDO), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	private void onMenuItemLog(ActionEvent event)
	{
		final LogViewer logViewer = AppContextHolder.getInstance().get(LogViewer.class);
		logViewer.showStage();
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

		projectSettingsViewController.showAndWait(new BaseProjectSettingsViewController.Param(projectController.getProject().getMetadata()), getContainingWindow());
	}

	public void onMenuItemSettings(ActionEvent event)
	{
		if(programSettingsViewController == null)
		{
			programSettingsViewController = AppContextHolder.getInstance().get(ProgramSettingsViewController.class);
		}

		programSettingsViewController.showAndWait(new BaseProgramSettingsViewController.Param(settingsController.getSettings(), settingsController.getOutputDevices()), getContainingWindow());
	}

	private void onColorChange(Pad pad, ModernColor color)
	{
		pad.setDefaultColor(color);
		updateStyle();
	}

	private void onColorSubmit(Set<UUID> padIds, ModernColor color)
	{
		try
		{
			client.currentProject().batchColorPads(padIds, color);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot color pads", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_COLOR_UPDATE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	public void onMenuItemReplaceMedia(ActionEvent event)
	{
		final List<ClientPadController> padControllersWithErrors = projectController.getPadControllersWithState(PadStatus.ERROR);
		final List<MissingMediaEntry> entries = padControllersWithErrors.stream()
				.map(ClientPadController::getPad)
				.map(pad -> MissingMediaEntry.builder()
						.pageName(projectController.getProject().getPageByPadId(pad.getId()).getSettings().getName())
						.padId(pad.getId())
						.padPosition(pad.getReadablePosition())
						.padName(pad.getName())
						.oldMediaPath(pad.getContent() instanceof AudioPadContent audioPadContent ? audioPadContent.getMediaPath() : null)
						.build())
				.toList();

		final ReplaceMediaViewController controller = AppContextHolder.getInstance().get(ReplaceMediaViewController.class);
		controller.setEntries(entries);
		controller.showStage();
	}
}
