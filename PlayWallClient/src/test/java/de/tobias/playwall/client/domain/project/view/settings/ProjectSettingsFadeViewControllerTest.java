package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.midi.mapping.Mapping;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.common.FadeSettingsController;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectSettingsFadeViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PROJECT_ID = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");

	private AppContext context;
	private Stage stage;

	private ProjectSettingsViewController projectSettingsViewController;
	private FadeSettingsController fadeViewController;

	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);
		context.registerLazySingleton(Client.class, _ -> client);
	}

	private ProjectMetadata createProjectMetadata(FadeSettings fadeSettings)
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
				.fadeSettings(fadeSettings)
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build();
	}

	private void openFadeSettingsTab(ProjectMetadata projectMetadata)
	{
		Platform.runLater(() -> {
			context.registerLazy(Stage.class, _ -> new Stage());
			projectSettingsViewController = context.get(ProjectSettingsViewController.class);
			fadeViewController = ((ProjectSettingsFadeViewController) projectSettingsViewController.selectCategory(3)).getFadeSettingsController();
			projectSettingsViewController.showAndWait(new BaseProjectSettingsViewController.Param(projectMetadata, null), stage);

		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	void testFadeSettingsInitializedFromProjectMetadata()
	{
		final FadeSettings fadeSettings = FadeSettings.builder()
				.fadeInDuration(2.5)
				.fadeInOnPlay(true)
				.fadeInOnResume(false)
				.fadeOutDuration(1.5)
				.fadeOutOnPause(true)
				.fadeOutOnStop(false)
				.fadeOutOnEndOfFile(true)
				.build();

		openFadeSettingsTab(createProjectMetadata(fadeSettings));

		assertThat(fadeViewController.getFadeInDurationSpinner().getEditor().getText()).isEqualTo("2,5");
		assertThat(fadeViewController.getFadeInPlayCheckBox().isSelected()).isTrue();
		assertThat(fadeViewController.getFadeInResumeCheckBox().isSelected()).isFalse();
		assertThat(fadeViewController.getFadeOutDurationSpinner().getEditor().getText()).isEqualTo("1,5");
		assertThat(fadeViewController.getFadeOutPauseCheckBox().isSelected()).isTrue();
		assertThat(fadeViewController.getFadeOutStopCheckBox().isSelected()).isFalse();
		assertThat(fadeViewController.getFadeOutEndOfFileCheckBox().isSelected()).isTrue();
	}

	@Test
	void testSavePreservesFadeSettingsWhenUnchanged(FxRobot robot) throws PlayWallApiException
	{
		openFadeSettingsTab(createProjectMetadata(new FadeSettings()));

		robot.clickOn("#saveButton");
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProjectSettings(ProjectMetadata.builder()
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
				.build());
	}

	@Test
	void testSaveWithCustomFadeSettingsValues(FxRobot robot) throws PlayWallApiException
	{
		openFadeSettingsTab(createProjectMetadata(new FadeSettings()));

		robot.interact(() -> {
			fadeViewController.getFadeInDurationSpinner().getEditor().setText("2,0");
			fadeViewController.getFadeOutDurationSpinner().getEditor().setText("3,0");
		});
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(fadeViewController.getFadeInPlayCheckBox());
		robot.clickOn(fadeViewController.getFadeOutStopCheckBox());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn("#saveButton");
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProjectSettings(ProjectMetadata.builder()
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
				.fadeSettings(FadeSettings.builder()
						.fadeInDuration(2.0)
						.fadeInOnPlay(true)
						.fadeInOnResume(false)
						.fadeOutDuration(3.0)
						.fadeOutOnPause(false)
						.fadeOutOnStop(true)
						.fadeOutOnEndOfFile(false)
						.build())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build());
	}

	@Test
	void testCancelDoesNotSaveFadeSettings(FxRobot robot) throws PlayWallApiException
	{
		openFadeSettingsTab(createProjectMetadata(new FadeSettings()));

		robot.interact(() -> robot.lookup("#fadeInPlayCheckBox").queryAs(CheckBox.class).fire());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn("#cancelButton");
		WaitForAsyncUtils.waitForFxEvents();

		verify(client, never()).updateProjectSettings(any());
	}
}
