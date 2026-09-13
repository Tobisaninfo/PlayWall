package de.tobias.playwall.client.domain.project.view.settings.mapping;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.mapping.action.GlobalVolumeAction;
import de.tobias.playwall.client.domain.mapping.action.StopAllAction;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.settings.BaseProjectSettingsViewController;
import de.tobias.playwall.client.domain.project.view.settings.ProjectSettingsViewController;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.application.Platform;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProjectSettingsMappingViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PROJECT_ID = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");

	private AppContext context;
	private Stage stage;

	private ProjectSettingsViewController projectSettingsViewController;
	private ProjectSettingsMappingViewController mappingViewController;

	private final Client client = mock(Client.class);
	private final ArgumentCaptor<ProjectMetadata> projectMetadataCaptor = ArgumentCaptor.forClass(ProjectMetadata.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);
		context.registerLazySingleton(Client.class, _ -> client);

		ClientProjectController clientProjectController = context.get(ClientProjectController.class);
		clientProjectController.loadProject(loadProject("projects/project_1.json"));
	}

	private ProjectMetadata createProjectMetadata()
	{
		return ProjectMetadata.builder()
				.id(PROJECT_ID)
				.name("Test Project")
				.numberOfHorizontalPads(4)
				.numberOfVerticalPads(3)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(null)
				.eofWarningTime(5.0)
				.fadeSettings(new FadeSettings())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build();
	}

	private void openSettingsTab(ProjectMetadata projectMetadata)
	{
		Platform.runLater(() -> {
			context.registerLazy(Stage.class, _ -> new Stage());
			projectSettingsViewController = context.get(ProjectSettingsViewController.class);
			mappingViewController = ((ProjectSettingsMappingViewController) projectSettingsViewController.selectCategory(4));
			projectSettingsViewController.showAndWait(new BaseProjectSettingsViewController.Param(projectMetadata, null), stage);

		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	// List

	@Test
	void testMappingListExistingKeys()
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(2);

		final IndexedCell<?>[] cells = getMappingListCells();
		assertThat(cellLabels(cells[0])).containsExactly("MIDI: 82", "Alle Kacheln stoppen");
		assertThat(cellLabels(cells[1])).containsExactly("Tastatur: K", "Globale Lautstärke (+5%)");
	}

	@Test
	void testMappingListEmptyKeys()
	{
		final ProjectMetadata projectMetadata = createProjectMetadata();
		openSettingsTab(projectMetadata);

		assertThat(mappingViewController.getMappingListView().getItems()).isEmpty();
	}

	@Test
	void testMappingListNoMapping()
	{
		final ProjectMetadata projectMetadata = createProjectMetadata();
		projectMetadata.setMappings(Map.of());
		projectMetadata.setSelectedMapping(null);

		openSettingsTab(projectMetadata);

		assertThat(mappingViewController.getMappingListView().getItems()).isEmpty();
	}

	@Test
	void testMappingListNoMappingSelected()
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		projectMetadata.setSelectedMapping(null);

		openSettingsTab(projectMetadata);

		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(2);
	}

	// Filter

	@Test
	void testMappingListFilterByText()
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> mappingViewController.getSearchTextField().setText("Globale Lautstärke"));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(1);

		final IndexedCell<?>[] cells = getMappingListCells();
		assertThat(cellLabels(cells[0])).containsExactly("Tastatur: K", "Globale Lautstärke (+5%)");

		// Clear filter

		Platform.runLater(() -> mappingViewController.getSearchTextField().clear());
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(2);
	}

	@Test
	void testMappingListFilterNoResults()
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> mappingViewController.getSearchTextField().setText("Seiten"));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getMappingListView().getItems()).isEmpty();
	}

	@Test
	void testMappingListFilterByKey(FxRobot robot)
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> robot.clickOn(mappingViewController.getSearchByKeyButton()));
		WaitForAsyncUtils.waitForFxEvents();

		robot.press(KeyCode.K);
		robot.release(KeyCode.K);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getSearchByKeyLabel().getText()).isEqualTo("K");
		assertThat(mappingViewController.getSearchByKeyClearButton().isVisible()).isTrue();

		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(1);
		final IndexedCell<?>[] cells = getMappingListCells();
		assertThat(cellLabels(cells[0])).containsExactly("Tastatur: K", "Globale Lautstärke (+5%)");

		// Clear filter

		Platform.runLater(() -> robot.clickOn(mappingViewController.getSearchByKeyClearButton()));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getSearchByKeyLabel().isVisible()).isFalse();
		assertThat(mappingViewController.getSearchByKeyClearButton().isVisible()).isFalse();
		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(2);
	}

	// Add Key

	@Test
	void testMappingListAddKey(FxRobot robot)
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> robot.clickOn(mappingViewController.getKeyboardAddButton()));
		WaitForAsyncUtils.waitForFxEvents();

		final Window keyDialogWindow = topWindow(robot);

		robot.press(KeyCode.A);
		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(robot.from(keyDialogWindow.getScene().getRoot()).lookup("Speichern").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(3);
		final IndexedCell<?>[] cells = getMappingListCells();
		assertThat(cellLabels(cells[1])).contains("Tastatur: A");
	}

	@Test
	void testMappingListAddExistingKey(FxRobot robot)
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> robot.clickOn(mappingViewController.getKeyboardAddButton()));
		WaitForAsyncUtils.waitForFxEvents();

		robot.press(KeyCode.K);
		robot.release(KeyCode.K);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup("Die Taste ist bereits vergeben").queryLabeled().isVisible()).isTrue();
	}

	// Remove Key

	@Test
	void testMappingListRemoveKey(FxRobot robot)
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		robot.clickOn(((HBox) getMappingListCells()[0].getGraphic()).getChildren().getLast());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getMappingListView().getItems()).hasSize(1);
	}

	// Action Mapping

	@Test
	void testSelectCorrectActionTab(FxRobot robot)
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> mappingViewController.getMappingListView().getSelectionModel().select(1)); // Keyboard K
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getActionTabs().getSelectionModel().getSelectedItem().getText()).isEqualTo("Globale Lautstärke");
		assertThat(robot.lookup("#modeComboBox").queryComboBox().getSelectionModel().getSelectedItem()).isEqualTo(GlobalVolumeAction.VolumeChangeMode.INCREASE);
		assertThat(robot.lookup("#deltaComboBox").queryComboBox().getSelectionModel().getSelectedItem()).isEqualTo(GlobalVolumeAction.VolumeChangeDelta.FIVE);
	}

	@Test
	void testChangeActionSettings(FxRobot robot)
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> mappingViewController.getMappingListView().getSelectionModel().select(1)); // Keyboard K
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mappingViewController.getActionTabs().getSelectionModel().getSelectedItem().getText()).isEqualTo("Globale Lautstärke");
		assertThat(robot.lookup("#modeComboBox").queryComboBox().getSelectionModel().getSelectedItem()).isEqualTo(GlobalVolumeAction.VolumeChangeMode.INCREASE);
		Platform.runLater(() -> {
			robot.lookup("#deltaComboBox").queryComboBox().getSelectionModel().select(GlobalVolumeAction.VolumeChangeDelta.TEN);
			mappingViewController.getMappingListView().getSelectionModel().select(0); // Select some different key
		});
		WaitForAsyncUtils.waitForFxEvents();

		final IndexedCell<?>[] cells = getMappingListCells();
		assertThat(cellLabels(cells[1])).containsExactly("Tastatur: K", "Globale Lautstärke (+10%)");
	}

	@Test
	void testChangeActionType()
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);

		Platform.runLater(() -> mappingViewController.getMappingListView().getSelectionModel().select(1)); // Keyboard K
		WaitForAsyncUtils.waitForFxEvents();

		// Check old tab
		assertThat(mappingViewController.getActionTabs().getSelectionModel().getSelectedItem().getText()).isEqualTo("Globale Lautstärke");

		// Switch tab
		Platform.runLater(() -> mappingViewController.getActionTabs().getSelectionModel().select(2));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(mappingViewController.getActionTabs().getSelectionModel().getSelectedItem().getText()).isEqualTo("Alle Kacheln stoppen");

		final IndexedCell<?>[] cells = getMappingListCells();
		assertThat(cellLabels(cells[1])).containsExactly("Tastatur: K", "Alle Kacheln stoppen");
	}

	// Save settings

	@Test
	void testSaveSettings(FxRobot robot) throws PlayWallApiException
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);
		assertThat(projectMetadata.getActiveMapping().getAllInputKeys()).hasSize(2);

		// Modifying
		Platform.runLater(() -> robot.clickOn(mappingViewController.getKeyboardAddButton()));
		WaitForAsyncUtils.waitForFxEvents();

		final Window keyDialogWindow = topWindow(robot);

		robot.press(KeyCode.A);
		robot.release(KeyCode.A);
		robot.clickOn(robot.from(keyDialogWindow.getScene().getRoot()).lookup("Speichern").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		// Saving
		robot.clickOn(robot.lookup("Speichern").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		// Verifying
		verify(client).updateProjectSettings(projectMetadataCaptor.capture());
		final ProjectMetadata saved = projectMetadataCaptor.getValue();
		assertThat(saved.getActiveMapping().getAllInputKeys()).hasSize(3);
	}

	@Test
	void testCancelSettings(FxRobot robot) throws PlayWallApiException
	{
		final ProjectMetadata projectMetadata = getProjectMetadataWithMapping();
		openSettingsTab(projectMetadata);
		assertThat(projectMetadata.getActiveMapping().getAllInputKeys()).hasSize(2);

		final Window settingsWindow = topWindow(robot);

		// Modifying
		Platform.runLater(() -> robot.clickOn(mappingViewController.getKeyboardAddButton()));
		WaitForAsyncUtils.waitForFxEvents();

		robot.press(KeyCode.A);
		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();

		// Saving
		robot.clickOn(robot.from(settingsWindow.getScene().getRoot()).lookup("Abbrechen").queryButton());

		// Verifying
		verify(client, never()).updateProjectSettings(any());
		assertThat(projectMetadata.getActiveMapping().getAllInputKeys()).hasSize(2);
	}

	// Utils

	private ProjectMetadata getProjectMetadataWithMapping()
	{
		final ProjectMetadata projectMetadata = createProjectMetadata();
		projectMetadata.getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.K, "K"), new GlobalVolumeAction(GlobalVolumeAction.VolumeChangeMode.INCREASE, GlobalVolumeAction.VolumeChangeDelta.FIVE));
		projectMetadata.getActiveMapping().addInputKeyWithAction(new MidiInputKey((byte) 82), new StopAllAction());
		return projectMetadata;
	}

	private IndexedCell<?>[] getMappingListCells()
	{
		return mappingViewController.getMappingListView().lookupAll(".cell").stream()
				.map(node -> (IndexedCell<?>) node)
				.filter(cell -> !cell.isEmpty())
				.sorted(Comparator.comparingInt(IndexedCell::getIndex))
				.toArray(IndexedCell<?>[]::new);
	}

	private static List<String> cellLabels(IndexedCell<?> cell)
	{
		final HBox hbox = (HBox) cell.getGraphic();
		final VBox vbox = (VBox) hbox.getChildren().getFirst();
		return vbox.getChildren().stream().map(node -> ((Label) node).getText()).toList();
	}

	/**
	 * NodeFinderImpl.rootsOfWindows() collects window roots into a {@link java.util.Set}, so a plain
	 * {@code robot.lookup(text)} is non-deterministic when the settings window and the key-input dialog
	 * are open at the same time and both contain a button with the same text (e.g. "Speichern"/"Abbrechen").
	 * Scoping the lookup to a specific window's root avoids that ambiguity.
	 */
	private static Window topWindow(FxRobot robot)
	{
		return robot.listWindows().getLast();
	}
}
