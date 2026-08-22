package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.MappingRegistry;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.device.MidiDeviceInfo;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.mapping.KeyNameLocalizer;
import de.tobias.playwall.client.domain.mapping.action.ActionDescription;
import de.tobias.playwall.client.domain.mapping.action.ActionSettingsViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.midi.event.MidiDeviceSelected;
import de.tobias.playwall.client.domain.project.view.KeyboardInputDialog;
import de.tobias.playwall.client.domain.project.view.settings.cell.InputKeyCell;
import de.tobias.playwall.client.domain.project.view.settings.cell.MidiDeviceInfoCell;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
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
	private static class InputKeyComparator implements Comparator<InputKey>
	{
		@Override
		public int compare(InputKey o1, InputKey o2)
		{
			return InputKeyLocalizer.localize(o1).compareTo(InputKeyLocalizer.localize(o2));
		}
	}

	private record ActionTab(Tab tab, int order, ActionSettingsViewController actionSettingsViewController)
	{
	}

	@FXML
	private ListView<InputKey> mappingListView;
	@FXML
	private TabPane actionTabs;

	@FXML
	private ComboBox<MidiDeviceInfo> midiDeviceComboBox;

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

	private final ClientProjectController projectController;
	private final UpdateMessageEventHandler eventHandler;
	private final Midi midi;
	private final MappingRegistry mappingRegistry;
	private final Map<Class<? extends Action>, ActionTab> actionTabMap = FXCollections.observableHashMap();

	private final ObservableList<InputKey> masterInputKeys = FXCollections.observableArrayList();
	private final FilteredList<InputKey> filteredInputKeys = new FilteredList<>(masterInputKeys, _ -> true);

	private KeyCode searchKeyCode;

	private Map<UUID, Mapping> mappings;
	private UUID selectedMapping;

	@InjectConstructor
	public ProjectSettingsMappingViewController(FluentClient client, UpdateMessageEventHandler eventHandler, ClientProjectController projectController, Midi midi, MappingRegistry mappingRegistry)
	{
		super(client);
		this.projectController = projectController;
		this.eventHandler = eventHandler;
		this.midi = midi;
		this.mappingRegistry = mappingRegistry;
	}

	@Override
	protected void init()
	{
		super.init();

		mappingListView.setCellFactory(_ -> new InputKeyCell(projectController.getProject(), getActiveMapping(), this::onInputKeyDeleted));
		mappingListView.getSelectionModel().selectedItemProperty()
				.addListener((_, oldValue, newValue) -> onInputKeySelected(oldValue, newValue));

		actionTabs.disableProperty().bind(mappingListView.getSelectionModel().selectedItemProperty().isNull());
		actionTabs.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> onTabChanged(newValue));

		midiDeviceComboBox.getItems().add(null);
		midiDeviceComboBox.getItems().addAll(midi.getMidiDevices());
		midiDeviceComboBox.setCellFactory(_ -> new MidiDeviceInfoCell());
		midiDeviceComboBox.setButtonCell(new MidiDeviceInfoCell());
		midiDeviceComboBox.getSelectionModel().selectedItemProperty().addListener((__, _, newValue) -> {
			try
			{
				eventHandler.fireEvent(new MidiDeviceSelected(newValue.name()));
			}
			catch(RuntimeException e)
			{
				// TODO Show error
				throw new RuntimeException(e);
			}
		});

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

			final ActionTab actionTab = new ActionTab(tab, actionDescription.order(), controller);
			tab.setUserData(actionTab);
			actionTabMap.put(action, actionTab);
		}

		actionTabMap.values().stream().sorted(Comparator.comparingInt(tab -> tab.order))
				.forEach(tab -> actionTabs.getTabs().add(tab.tab));
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
		actionTab.actionSettingsViewController.applySettings(activeMapping.getAction(getSelectedKey()));

		param.getProjectMetadata().setMidiDevice(Optional.ofNullable(midiDeviceComboBox.getSelectionModel().getSelectedItem())
				.map(MidiDeviceInfo::name)
				.orElse(null));
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
			mappingListView.getSelectionModel().select(key);
		});
	}

	@FXML
	private void onMidiAdd()
	{
		final Mapping mapping = getActiveMapping();
		if(mapping == null)
		{
			return;
		}

		MidiInputKey key = new MidiInputKey((byte) 36);
		mapping.addInputKeyWithAction(key, null);
		updateInputListView();
		mappingListView.getSelectionModel().select(key);
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
		final Action newAction = actionTab.actionSettingsViewController.createNewAction();
		mapping.addInputKeyWithAction(selectedKey, newAction);
		actionTab.actionSettingsViewController.initSettings(newAction);
		updateInputListView();
	}

	private void onInputKeySelected(InputKey oldValue, InputKey newValue)
	{
		final Mapping mapping = getActiveMapping();
		if(mapping == null)
		{
			return;
		}

		// Save old action
		if(oldValue != null)
		{
			final ActionTab oldActionTab = (ActionTab) actionTabs.getSelectionModel().getSelectedItem().getUserData();
			oldActionTab.actionSettingsViewController.applySettings(mapping.getAction(oldValue));
		}

		// Show new action
		if(newValue != null)
		{
			final Action action = mapping.getAction(newValue);
			if(action != null)
			{
				final ActionTab newActionTab = actionTabMap.get(action.getClass());
				actionTabs.getSelectionModel().select(newActionTab.tab);
				newActionTab.actionSettingsViewController.initSettings(action);
			}
			else
			{
				// Create a new action if none exists by using the current selected tab
				final ActionTab actionTab = (ActionTab) actionTabs.getSelectionModel().getSelectedItem().getUserData();
				mapping.addInputKeyWithAction(newValue, actionTab.actionSettingsViewController.createNewAction());
				actionTab.actionSettingsViewController.initSettings(mapping.getAction(newValue));
			}
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
			final List<InputKey> sortedInputKeys = new ArrayList<>(mapping.getAllInputKeys());
			sortedInputKeys.sort(new InputKeyComparator());
			masterInputKeys.setAll(sortedInputKeys);
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
