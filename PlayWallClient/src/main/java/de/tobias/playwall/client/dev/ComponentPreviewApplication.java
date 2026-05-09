package de.tobias.playwall.client.dev;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.loader.AppContextLoader;
import de.tobias.playwall.client.view.style.Styleable;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class ComponentPreviewApplication extends Application
{
	private static final double ITEM_SPACING = 8;
	private static final double SECTION_SPACING = 4;
	private static final double ROOT_SPACING = 20;

	public static void main(String[] args)
	{
		App app = ApplicationUtils.registerMainApplication(ComponentPreviewApplication.class);
		app.start(args);
	}

	@Override
	public void init() throws Exception
	{
		try
		{
			final AppContext appContext = new AppContext(AppContext.Environment.PRODUCTION);
			appContext.registerLazySingleton(App.class, _ -> ApplicationUtils.getApplication());
			AppContextLoader.setupDependencies(appContext);
			AppContextHolder.setInstance(appContext);
		}
		catch(Exception e)
		{
			System.err.println(e);
			throw e;
		}
	}

	@Override
	public void start(Stage stage)
	{
		VBox root = new VBox(ROOT_SPACING);
		root.setPadding(new Insets(24));

		root.getChildren().addAll(
				section("Button", buttons()),
				new Separator(),
				section("CheckBox", checkBoxes()),
				new Separator(),
				section("RadioButton", radioButtons()),
				new Separator(),
				section("ToggleButton", toggleButtons()),
				new Separator(),
				section("TextField", textFields()),
				new Separator(),
				section("TextArea", textAreas()),
				new Separator(),
				section("ComboBox", comboBoxes()),
				new Separator(),
				section("Spinner", spinners()),
				new Separator(),
				section("Slider", sliders()),
				new Separator(),
				section("ProgressBar", progressBars()),
				new Separator(),
				section("ProgressIndicator", progressIndicators()),
				new Separator(),
				section("TabPane", tabPane()),
				new Separator(),
				section("ListView", listView()),
				new Separator(),
				section("Label", labels()),
				new Separator(),
				section("MenuBar", menuBar()),
				new Separator(),
				section("MenuButton", menuButtons()),
				new Separator(),
				section("ContextMenu", contextMenuTrigger()),
				new Separator(),
				section("TableView", tableView()),
				new Separator(),
				section("TreeView", treeView())
		);

		ScrollPane scrollPane = new ScrollPane(root);
		scrollPane.setFitToWidth(true);

		Scene scene = new Scene(scrollPane, 860, 750);

		stage.setScene(scene);
		stage.setTitle("Component Preview");

		final ObservableMap<KeyCombination, Runnable> accelerators = stage.getScene().getAccelerators();
		final Runnable openDevTools = () -> ScenicView.show(stage.getScene());
		accelerators.put(new KeyCodeCombination(KeyCode.F12, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), openDevTools);
		AppContextHolder.getInstance().get(Styleable.class).applyToStage(stage);

		stage.show();
	}

	private VBox section(String title, Node content)
	{
		Label label = new Label(title);
		label.getStyleClass().add("settings-entry-label");
		return new VBox(SECTION_SPACING, label, content);
	}

	private HBox buttons()
	{
		Button normal = new Button("Normal");

		Button defaultBtn = new Button("Default");
		defaultBtn.setDefaultButton(true);

		Button danger = new Button("Danger");
		danger.getStyleClass().add("danger");

		Button disabled = new Button("Disabled");
		disabled.setDisable(true);

		Button dangerDisabled = new Button("Danger Disabled");
		dangerDisabled.getStyleClass().add("danger");
		dangerDisabled.setDisable(true);

		return new HBox(ITEM_SPACING, normal, defaultBtn, danger, disabled, dangerDisabled);
	}

	private HBox checkBoxes()
	{
		CheckBox normal = new CheckBox("Normal");

		CheckBox selected = new CheckBox("Selected");
		selected.setSelected(true);

		CheckBox indeterminate = new CheckBox("Indeterminate");
		indeterminate.setIndeterminate(true);

		CheckBox disabled = new CheckBox("Disabled");
		disabled.setDisable(true);

		CheckBox selectedDisabled = new CheckBox("Selected & Disabled");
		selectedDisabled.setSelected(true);
		selectedDisabled.setDisable(true);

		return new HBox(ITEM_SPACING * 2, normal, selected, indeterminate, disabled, selectedDisabled);
	}

	private HBox radioButtons()
	{
		ToggleGroup group1 = new ToggleGroup();
		RadioButton optionA = new RadioButton("Option A");
		optionA.setToggleGroup(group1);
		RadioButton optionB = new RadioButton("Option B");
		optionB.setToggleGroup(group1);
		optionB.setSelected(true);

		RadioButton disabled = new RadioButton("Disabled");
		disabled.setDisable(true);

		RadioButton selectedDisabled = new RadioButton("Selected & Disabled");
		selectedDisabled.setSelected(true);
		selectedDisabled.setDisable(true);

		return new HBox(ITEM_SPACING * 2, optionA, optionB, disabled, selectedDisabled);
	}

	private HBox toggleButtons()
	{
		ToggleButton off = new ToggleButton("Off");

		ToggleButton on = new ToggleButton("On");
		on.setSelected(true);

		ToggleButton disabled = new ToggleButton("Disabled");
		disabled.setDisable(true);

		ToggleButton selectedDisabled = new ToggleButton("Selected & Disabled");
		selectedDisabled.setSelected(true);
		selectedDisabled.setDisable(true);

		return new HBox(ITEM_SPACING, off, on, disabled, selectedDisabled);
	}

	private VBox textFields()
	{
		TextField normal = new TextField("Normal text");
		normal.setMaxWidth(280);

		TextField prompt = new TextField();
		prompt.setPromptText("Prompt text...");
		prompt.setMaxWidth(280);

		TextField disabled = new TextField("Disabled");
		disabled.setDisable(true);
		disabled.setMaxWidth(280);

		return new VBox(ITEM_SPACING, normal, prompt, disabled);
	}

	private HBox textAreas()
	{
		TextArea normal = new TextArea("Normal text area content.");
		normal.setMaxWidth(280);
		normal.setPrefRowCount(3);
		normal.setWrapText(true);

		TextArea disabled = new TextArea("Disabled text area.");
		disabled.setMaxWidth(280);
		disabled.setPrefRowCount(3);
		disabled.setWrapText(true);
		disabled.setDisable(true);

		return new HBox(ITEM_SPACING, normal, disabled);
	}

	private HBox comboBoxes()
	{
		ComboBox<String> normal = new ComboBox<>();
		normal.getItems().addAll("Option 1", "Option 2", "Option 3");
		normal.setValue("Option 1");

		ComboBox<String> disabled = new ComboBox<>();
		disabled.getItems().addAll("Option 1", "Option 2");
		disabled.setValue("Option 1");
		disabled.setDisable(true);

		return new HBox(ITEM_SPACING, normal, disabled);
	}

	private HBox spinners()
	{
		Spinner<Integer> normal = new Spinner<>(0, 100, 42);
		normal.setEditable(true);

		Spinner<Integer> disabled = new Spinner<>(0, 100, 42);
		disabled.setDisable(true);

		return new HBox(ITEM_SPACING, normal, disabled);
	}

	private VBox sliders()
	{
		Slider normal = new Slider(0, 100, 40);
		normal.setMaxWidth(300);
		normal.setShowTickLabels(true);
		normal.setShowTickMarks(true);

		Slider disabled = new Slider(0, 100, 70);
		disabled.setMaxWidth(300);
		disabled.setDisable(true);

		return new VBox(ITEM_SPACING, normal, disabled);
	}

	private VBox progressBars()
	{
		ProgressBar zero = new ProgressBar(0);
		zero.setMaxWidth(300);

		ProgressBar half = new ProgressBar(0.5);
		half.setMaxWidth(300);

		ProgressBar full = new ProgressBar(1.0);
		full.setMaxWidth(300);

		ProgressBar indeterminate = new ProgressBar(-1);
		indeterminate.setMaxWidth(300);

		return new VBox(ITEM_SPACING, zero, half, full, indeterminate);
	}

	private HBox progressIndicators()
	{
		ProgressIndicator determinate = new ProgressIndicator(0.65);

		ProgressIndicator indeterminate = new ProgressIndicator(-1);

		ProgressIndicator disabled = new ProgressIndicator(0.3);
		disabled.setDisable(true);

		return new HBox(ITEM_SPACING * 4, determinate, indeterminate, disabled);
	}

	private TabPane tabPane()
	{
		TabPane tabPane = new TabPane();
		tabPane.setMaxWidth(400);

		Tab tab1 = new Tab("Tab 1", new Label("Content of Tab 1"));
		tab1.setClosable(false);

		Tab tab2 = new Tab("Tab 2", new Label("Content of Tab 2"));
		tab2.setClosable(false);

		Tab tab3 = new Tab("Disabled");
		tab3.setClosable(false);
		tab3.setDisable(true);

		tabPane.getTabs().addAll(tab1, tab2, tab3);
		return tabPane;
	}

	private ListView<String> listView()
	{
		ListView<String> listView = new ListView<>();
		listView.getItems().addAll("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
		listView.setMaxHeight(130);
		listView.setMaxWidth(280);
		return listView;
	}

	private VBox labels()
	{
		Label normal = new Label("Normal Label");

		Label disabled = new Label("Disabled Label");
		disabled.setDisable(true);

		return new VBox(ITEM_SPACING, normal, disabled);
	}

	private MenuBar menuBar()
	{
		MenuBar menuBar = new MenuBar();

		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(
				new MenuItem("Open"),
				new MenuItem("Save"),
				new SeparatorMenuItem(),
				disabledItem("Export (Disabled)"),
				new SeparatorMenuItem(),
				dangerItem("Delete")
		);

		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(
				new MenuItem("Cut"),
				new MenuItem("Copy"),
				new MenuItem("Paste")
		);

		menuBar.getMenus().addAll(fileMenu, editMenu);
		return menuBar;
	}

	private MenuItem disabledItem(String text)
	{
		MenuItem item = new MenuItem(text);
		item.setDisable(true);
		return item;
	}

	private MenuItem dangerItem(String text)
	{
		MenuItem item = new MenuItem(text);
		item.getStyleClass().add("danger");
		return item;
	}

	private HBox menuButtons()
	{
		MenuButton normal = new MenuButton("Menu Button");
		normal.getItems().addAll(
				new MenuItem("Option 1"),
				new MenuItem("Option 2"),
				new SeparatorMenuItem(),
				dangerItem("Delete")
		);

		MenuButton disabled = new MenuButton("Disabled");
		disabled.getItems().add(new MenuItem("Option 1"));
		disabled.setDisable(true);

		return new HBox(ITEM_SPACING, normal, disabled);
	}

	private HBox contextMenuTrigger()
	{
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.getItems().addAll(
				new MenuItem("Action 1"),
				new MenuItem("Action 2"),
				new SeparatorMenuItem(),
				disabledItem("Disabled Action"),
				new SeparatorMenuItem(),
				dangerItem("Delete")
		);

		Button trigger = new Button("Right-click for ContextMenu");
		trigger.setOnAction(e -> contextMenu.show(trigger, javafx.geometry.Side.BOTTOM, 0, 4));
		trigger.setContextMenu(contextMenu);

		return new HBox(trigger);
	}

	@SuppressWarnings("unchecked")
	private TableView<String[]> tableView()
	{
		TableView<String[]> table = new TableView<>();
		table.setMaxWidth(500);
		table.setMaxHeight(160);

		TableColumn<String[], String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[0]));
		nameCol.setPrefWidth(160);

		TableColumn<String[], String> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[1]));
		statusCol.setPrefWidth(120);

		TableColumn<String[], String> valueCol = new TableColumn<>("Value");
		valueCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[2]));
		valueCol.setPrefWidth(100);

		table.getColumns().addAll(nameCol, statusCol, valueCol);
		table.getItems().addAll(
				new String[]{"Alpha", "Active", "42"},
				new String[]{"Beta", "Inactive", "7"},
				new String[]{"Gamma", "Active", "99"},
				new String[]{"Delta", "Pending", "13"}
		);

		return table;
	}

	private TreeView<String> treeView()
	{
		TreeItem<String> root = new TreeItem<>("Root");
		root.setExpanded(true);

		TreeItem<String> group1 = new TreeItem<>("Group A");
		group1.setExpanded(true);
		group1.getChildren().addAll(new TreeItem<>("Item A1"), new TreeItem<>("Item A2"));

		TreeItem<String> group2 = new TreeItem<>("Group B");
		group2.getChildren().addAll(new TreeItem<>("Item B1"), new TreeItem<>("Item B2"), new TreeItem<>("Item B3"));

		root.getChildren().addAll(group1, group2, new TreeItem<>("Standalone Item"));

		TreeView<String> treeView = new TreeView<>(root);
		treeView.setMaxWidth(280);
		treeView.setMaxHeight(180);
		return treeView;
	}
}
