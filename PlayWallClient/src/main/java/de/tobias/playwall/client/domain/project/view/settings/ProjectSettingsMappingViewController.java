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
import de.tobias.playwall.client.domain.mapping.action.ActionSettingsViewController;
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

import java.util.*;

/**
 * Viewcontroller for the mapping page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsMappingPageView", applyToStage = false)
public class ProjectSettingsMappingViewController extends BaseProjectSettingsViewController
{
	private record ActionTab(Tab tab, ActionSettingsViewController actionSettingsViewController)
	{
	}

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
	private final Map<Class<? extends Action>, ActionTab> actionTabMap = FXCollections.observableHashMap();

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

		mappingListView.setCellFactory(_ -> new MappingKeyCell(getActiveMapping(), this::onInputKeyDeleted));
		mappingListView.getSelectionModel().selectedItemProperty()
				.addListener((_, oldValue, newValue) -> onInputKeySelected(oldValue, newValue));

		actionTabs.disableProperty().bind(mappingListView.getSelectionModel().selectedItemProperty().isNull());
		actionTabs.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> onTabChanged(newValue));

		mappingListView.setItems(filteredInputKeys);
		searchTextField.textProperty().addListener((_, _, newValue) ->
		{
			if(newValue != null && !newValue.isBlank())
			{
				onSearchByKeyClear();
			}
			updateSearchPredicate();
		});

		for(Class<? extends Action> action : mappingRegistry.getRegisteredActions())
		{
			final ActionDescription actionDescription = action.getAnnotation(ActionDescription.class);
			final ActionSettingsViewController controller = AppContextHolder.getInstance().get(actionDescription.settingsViewController());
			final Tab tab = new Tab(Localization.getString(actionDescription.nameKey()), controller.getParent());

			final ActionTab actionTab = new ActionTab(tab, controller);
			tab.setUserData(actionTab);
			actionTabMap.put(action, actionTab);
			actionTabs.getTabs().add(tab);
		}
	}

	@Override
	public void initParameter(Param parameter)
	{
		this.isValidProperty.set(true);

		mappings = new HashMap<>();
		for(Map.Entry<UUID, Mapping> entry : parameter.getProjectMetadata().getMappings().entrySet())
		{
			mappings.put(entry.getKey(), entry.getValue().copy());
		}

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
		final Mapping activeMapping = getActiveMapping();
		if(activeMapping == null)
		{
			return;
		}
		final ActionTab actionTab = (ActionTab) actionTabs.getSelectionModel().getSelectedItem().getUserData();
		actionTab.actionSettingsViewController.saveAction(activeMapping.getAction(getSelectedKey()));

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
		final Mapping mapping = getActiveMapping();
		if(mapping == null)
		{
			return;
		}

		final KeyboardInputDialog dialog = AppContextHolder.getInstance().get(KeyboardInputDialog.class);
		final Optional<KeyboardInputKey> result = dialog.showAndWait(new KeyboardInputDialog.Param(mapping, false), getContainingWindow());
		result.ifPresent(key -> {
			mapping.addInputKeyWithAction(key, null);
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

	private InputKey getSelectedKey()
	{
		return mappingListView.getSelectionModel().getSelectedItem();
	}

	private void onTabChanged(Tab newTab)
	{
		final Mapping mapping = getActiveMapping();
		if(mapping == null)
		{
			return;
		}

		final InputKey selectedKey = getSelectedKey();

		final ActionTab actionTab = (ActionTab) newTab.getUserData();
		mapping.addInputKeyWithAction(selectedKey, actionTab.actionSettingsViewController.createNewAction());
	}

	private void onInputKeySelected(InputKey oldValue, InputKey newValue)
	{
		final Mapping mapping = getActiveMapping();
		if(mapping == null)
		{
			return;
		}

		// Save old action
		final ActionTab oldActionTab = (ActionTab) actionTabs.getSelectionModel().getSelectedItem().getUserData();
		oldActionTab.actionSettingsViewController.saveAction(mapping.getAction(oldValue));

		// Show new action
		final Action action = mapping.getAction(newValue);
		if(action != null)
		{
			final ActionTab newActionTab = actionTabMap.get(action.getClass());
			actionTabs.getSelectionModel().select(newActionTab.tab);
			newActionTab.actionSettingsViewController.initAction(action);
		}
		else
		{
			// Create a new action if none exists by using the current selected tab
			final ActionTab actionTab = (ActionTab) actionTabs.getSelectionModel().getSelectedItem().getUserData();
			mapping.addInputKeyWithAction(newValue, actionTab.actionSettingsViewController.createNewAction());
		}
	}

	private void onInputKeyDeleted(InputKey key)
	{
		final Mapping mapping = getActiveMapping();
		if(mapping != null)
		{
			mapping.removeInputKey(key);
			updateInputListView();
		}
	}

	private Mapping getActiveMapping()
	{
		if(mappings == null || selectedMapping == null)
		{
			return null;
		}
		return mappings.get(selectedMapping);
	}

	private void updateInputListView()
	{
		final Mapping mapping = getActiveMapping();
		if(mapping != null)
		{
			masterInputKeys.setAll(mapping.getAllInputKeys());
		}
	}

	private boolean matchesSearch(InputKey key, String query)
	{
		if(query == null || query.isBlank())
		{
			return true;
		}
		final Mapping mapping = getActiveMapping();
		if(mapping == null)
		{
			return false;
		}

		final Action action = mapping.getAction(key);
		if(action == null)
		{
			return false;
		}

		return action.toString().toLowerCase().contains(query.toLowerCase());
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
