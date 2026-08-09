package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.MappingRegistry;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.mapping.KeyNameLocalizer;
import de.tobias.playwall.client.domain.mapping.action.ActionDescription;
import de.tobias.playwall.client.domain.project.view.KeyboardInputDialog;
import de.tobias.playwall.client.domain.project.view.settings.cell.MappingKeyCell;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Viewcontroller for the mapping page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsMappingPageView", applyToStage = false)
public class ProjectSettingsMappingViewController extends BaseProjectSettingsViewController
{
	@FXML
	private ListView<InputKey> mappingListView;
	@FXML
	private TabPane actionTabs;

	@FXML
	private TextField searchTextField;

	@FXML
	private PlayWallButton searchByKeyButton;

	@FXML
	private PlayWallButton searchByKeyClearButton;

	@FXML
	private Label searchByKeyLabel;

	@FXML
	private PlayWallButton keyboardAddButton;
	@FXML
	private PlayWallButton midiAddButton;

	private final MappingRegistry mappingRegistry;

	private final ObservableList<InputKey> masterInputKeys = FXCollections.observableArrayList();
	private final FilteredList<InputKey> filteredInputKeys = new FilteredList<>(masterInputKeys, _ -> true);

	private KeyCode searchKeyCode;

	private Map<UUID, Mapping> mappings;
	private UUID selectedMapping;

	@InjectConstructor
	public ProjectSettingsMappingViewController(FluentClient client, MappingRegistry mappingRegistry)
	{
		super(client);
		this.mappingRegistry = mappingRegistry;
	}

	@Override
	protected void init()
	{
		super.init();

		this.searchTextField.setPromptText(Localization.getString("ui.settings.project.mapping.search.prompt"));

		mappingListView.setCellFactory(_ -> new MappingKeyCell(getActiveMapping(), this::onInputKeyDeleted));
		mappingListView.getSelectionModel().selectedItemProperty()
				.addListener((_, _, newValue) -> onInputKeySelected(newValue));

		mappingListView.setItems(filteredInputKeys);
		searchTextField.textProperty().addListener((_, _, newValue) ->
		{
			if(newValue != null && !newValue.isBlank())
			{
				onSearchByKeyClear();
			}
			updateSearchPredicate();
		});

		searchByKeyButton.getStyleClass().add("project-settings--search-by-key-button");
		searchByKeyLabel.getStyleClass().addAll("key-input-label", "project-settings--search-by-key-label");

		for(Class<? extends Action> action : mappingRegistry.getRegisteredActions())
		{
			final ActionDescription actionDescription = action.getAnnotation(ActionDescription.class);
			final Tab tab = new Tab(Localization.getString(actionDescription.nameKey()), new VBox());
			tab.setUserData(action);
			actionTabs.getTabs().add(tab);
		}
	}

	@Override
	public void initParameter(Param parameter)
	{
		this.isValidProperty.set(true);

		mappings = parameter.getProjectMetadata().getMappings();
		selectedMapping = parameter.getProjectMetadata().getSelectedMapping();

		if(mappings.isEmpty())
		{
			mappings.put(UUID.randomUUID(), new Mapping());
			selectedMapping = mappings.keySet().iterator().next();
		}
		if(selectedMapping == null)
		{
			selectedMapping = mappings.keySet().iterator().next();
		}

		updateInputListView();
	}

	@Override
	public void applySettings(Param param)
	{
		param.getProjectMetadata().setMappings(mappings);
		param.getProjectMetadata().setSelectedMapping(selectedMapping);
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}

	// Event Handler

	@FXML
	private void onKeyboardAdd()
	{
		final KeyboardInputDialog dialog = AppContextHolder.getInstance().get(KeyboardInputDialog.class);
		final Optional<KeyboardInputKey> result = dialog.showAndWait(new KeyboardInputDialog.Param(getActiveMapping(), false), getContainingWindow());
		result.ifPresent(key -> {
			getActiveMapping().addInputKeyWithAction(key, null);
			updateInputListView();
		});
	}

	@FXML
	private void onMidiAdd()
	{

	}

	@FXML
	private void onSearchByKey()
	{
		final KeyboardInputDialog dialog = AppContextHolder.getInstance().get(KeyboardInputDialog.class);
		final Optional<KeyboardInputKey> result = dialog.showAndWait(new KeyboardInputDialog.Param(getActiveMapping(), true), getContainingWindow());
		result.ifPresent(key ->
		{
			searchTextField.clear();

			searchKeyCode = key.code();
			searchByKeyLabel.setText(KeyNameLocalizer.getKeyName(key.code()));
			searchByKeyLabel.setVisible(true);
			searchByKeyClearButton.setVisible(true);
			updateSearchPredicate();
		});
	}

	@FXML
	private void onSearchByKeyClear()
	{
		searchKeyCode = null;
		searchByKeyLabel.setText(null);
		searchByKeyLabel.setVisible(false);
		searchByKeyClearButton.setVisible(false);
		updateSearchPredicate();
	}

	private void onInputKeySelected(InputKey key)
	{
		final Mapping mapping = getActiveMapping();
		final Action action = mapping.getAction(key);
		if(action != null)
		{
			actionTabs.getSelectionModel().select(actionTabs.getTabs().stream()
					.filter(tab -> tab.getUserData().equals(action.getClass()))
					.findFirst()
					.orElse(null));
		}
	}

	private void onInputKeyDeleted(InputKey key)
	{
		final Mapping mapping = getActiveMapping();
		mapping.removeInputKey(key);
		updateInputListView();
	}

	private Mapping getActiveMapping()
	{
		return mappings.get(selectedMapping);
	}

	private void updateInputListView()
	{
		masterInputKeys.setAll(getActiveMapping().getAllInputKeys());
	}

	private boolean matchesSearch(InputKey key, String query)
	{
		if(query == null || query.isBlank())
		{
			return true;
		}
		final Action action = getActiveMapping().getAction(key);
		return action != null && action.toString().toLowerCase().contains(query.toLowerCase());
	}

	private void updateSearchPredicate()
	{
		filteredInputKeys.setPredicate(key ->
		{
			if(searchKeyCode != null)
			{
				return matchesKey(key, searchKeyCode);
			}
			return matchesSearch(key, searchTextField.getText());
		});
	}

	private boolean matchesKey(InputKey key, KeyCode keyCode)
	{
		return switch(key)
		{
			case KeyboardInputKey k -> k.key() != null && k.key().equalsIgnoreCase(keyCode.getName());
			case MidiInputKey m -> false;
			default -> throw new IllegalStateException("Unexpected value: " + key);
		};
	}
}
