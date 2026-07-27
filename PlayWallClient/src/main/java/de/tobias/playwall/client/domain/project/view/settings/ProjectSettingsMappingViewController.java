package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallButton;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

import java.util.Map;
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
	private PlayWallButton keyboardAddButton;
	@FXML
	private PlayWallButton midiAddButton;

	private Map<UUID, Mapping> mappings;
	private UUID selectedMapping;

	@InjectConstructor
	public ProjectSettingsMappingViewController(FluentClient client)
	{
		super(client);
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
		getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.E, "E"), null);
		updateInputListView();
	}

	@FXML
	private void onMidiAdd()
	{

	}

	private Mapping getActiveMapping()
	{
		return mappings.get(selectedMapping);
	}

	private void updateInputListView()
	{
		mappingListView.getItems().setAll(getActiveMapping().getAllInputKeys());
	}
}
