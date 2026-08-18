package de.tobias.playwall.client.domain.pad.view.settings;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PadSettingsFadeViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PAD_ID = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

	private AppContext context;
	private Stage stage;

	private PadSettingsViewController padSettingsViewController;
	private PadSettingsFadeViewController padSettingsFadeViewController;

	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);
		context.registerLazySingleton(Client.class, _ -> client);

		final Project project = loadProject("projects/project_1.json");
		context.get(ClientProjectController.class).loadProject(project);
	}

	private Pad createPadWithoutFadeSettings()
	{
		return Pad.builder()
				.id(PAD_ID)
				.position(0)
				.name("Test Pad")
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.build())
				.build();
	}

	private void openFadeSettingsTab(Pad pad)
	{
		Platform.runLater(() -> {
			context.registerLazy(Stage.class, _ -> new Stage());
			padSettingsViewController = context.get(PadSettingsViewController.class);
			padSettingsFadeViewController = (PadSettingsFadeViewController) padSettingsViewController.selectCategory(3);
			padSettingsViewController.showAndWait(new BasePadSettingsViewController.Param(pad, padSettingsViewController), stage);
		});

		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	void testFadeSettingsDisabledByDefaultWhenPadHasNoFadeSettings()
	{
		openFadeSettingsTab(createPadWithoutFadeSettings());

		assertThat(padSettingsFadeViewController.getEnableCheckbox().isSelected()).isFalse();
	}

	@Test
	void testEnableCheckboxEnablesFadeControls(FxRobot robot)
	{
		openFadeSettingsTab(createPadWithoutFadeSettings());

		final CheckBox fadeInPlayCheckBox = padSettingsFadeViewController.getFadeSettingsController().getFadeInPlayCheckBox();

		assertThat(padSettingsFadeViewController.getEnableCheckbox().isSelected()).isFalse();
		assertThat(fadeInPlayCheckBox.isDisabled()).isTrue();

		robot.clickOn(padSettingsFadeViewController.getEnableCheckbox());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padSettingsFadeViewController.getEnableCheckbox().isSelected()).isTrue();
		assertThat(fadeInPlayCheckBox.isDisabled()).isFalse();
	}

	@Test
	void testDisableCheckboxDisablesFadeControls(FxRobot robot)
	{
		openFadeSettingsTab(createPadWithoutFadeSettings());

		final CheckBox fadeInPlayCheckBox = padSettingsFadeViewController.getFadeSettingsController().getFadeInPlayCheckBox();

		robot.clickOn(padSettingsFadeViewController.getEnableCheckbox());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padSettingsFadeViewController.getEnableCheckbox().isSelected()).isTrue();
		assertThat(fadeInPlayCheckBox.isDisabled()).isFalse();

		robot.clickOn(padSettingsFadeViewController.getEnableCheckbox());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padSettingsFadeViewController.getEnableCheckbox().isSelected()).isFalse();
		assertThat(fadeInPlayCheckBox.isDisabled()).isTrue();
	}

	@Test
	void testSaveWithCheckboxUncheckedSetsFadeSettingsToNull(FxRobot robot) throws PlayWallApiException
	{
		final Pad pad = createPadWithoutFadeSettings();
		openFadeSettingsTab(pad);

		assertThat(padSettingsFadeViewController.getEnableCheckbox().isSelected()).isFalse();

		robot.clickOn("#saveButton");
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateSettings(PAD_ID, Pad.builder()
				.name("Test Pad")
				.id(PAD_ID)
				.position(0)
				.timeMode(null)
				.defaultColor(null)
				.playColor(null)
				.introColor(null)
				.eofWarningTime(null)
				.introDuration(0.0)
				.fadeSettings(null)
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.build())
				.build());
	}

	@Test
	void testSaveWithCheckboxCheckedSetsFadeSettings(FxRobot robot) throws PlayWallApiException
	{
		final Pad pad = createPadWithoutFadeSettings();
		openFadeSettingsTab(pad);

		robot.clickOn(padSettingsFadeViewController.getEnableCheckbox());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn("#saveButton");
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateSettings(PAD_ID, Pad.builder()
				.name("Test Pad")
				.id(PAD_ID)
				.position(0)
				.timeMode(null)
				.defaultColor(null)
				.playColor(null)
				.introColor(null)
				.eofWarningTime(null)
				.introDuration(0.0)
				.fadeSettings(new FadeSettings())
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.build())
				.build());
	}

	@Test
	void testSaveWithCustomFadeSettingsValues(FxRobot robot) throws PlayWallApiException
	{
		final Pad pad = createPadWithoutFadeSettings();
		openFadeSettingsTab(pad);

		robot.clickOn(padSettingsFadeViewController.getEnableCheckbox());
		WaitForAsyncUtils.waitForFxEvents();

		robot.interact(() -> {
			padSettingsFadeViewController.getFadeSettingsController().getFadeInDurationSpinner().getEditor().setText("2,0");
			padSettingsFadeViewController.getFadeSettingsController().getFadeOutDurationSpinner().getEditor().setText("3,0");
		});
		WaitForAsyncUtils.waitForFxEvents();

		final CheckBox fadeInPlayCheckBox = padSettingsFadeViewController.getFadeSettingsController().getFadeInPlayCheckBox();
		final CheckBox fadeOutStopCheckBox = padSettingsFadeViewController.getFadeSettingsController().getFadeOutStopCheckBox();
		robot.clickOn(fadeInPlayCheckBox);
		robot.clickOn(fadeOutStopCheckBox);
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn("#saveButton");
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateSettings(PAD_ID, Pad.builder()
				.name("Test Pad")
				.id(PAD_ID)
				.position(0)
				.timeMode(null)
				.defaultColor(null)
				.playColor(null)
				.introColor(null)
				.eofWarningTime(null)
				.introDuration(0.0)
				.fadeSettings(FadeSettings.builder()
						.fadeInDuration(2.0)
						.fadeInOnPlay(true)
						.fadeInOnResume(false)
						.fadeOutDuration(3.0)
						.fadeOutOnPause(false)
						.fadeOutOnStop(true)
						.fadeOutOnEndOfFile(false)
						.build())
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.build())
				.build());
	}
}
