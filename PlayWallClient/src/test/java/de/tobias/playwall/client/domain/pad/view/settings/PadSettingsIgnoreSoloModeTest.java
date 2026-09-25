package de.tobias.playwall.client.domain.pad.view.settings;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.settings.content.AudioPadContentSettingsContainer;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PadSettingsIgnoreSoloModeTest extends AbstractViewControllerTest
{
	private static final UUID PAD_ID = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

	private AppContext context;
	private Stage stage;

	private PadSettingsGeneralViewController padSettingsGeneralViewController;

	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);
		context.registerLazySingleton(Client.class, _ -> client);
	}

	@Test
	void testIgnoreSoloModeSettingsDisabledInMultiMode(FxRobot robot)
	{
		openPadSettings(createPad(true), false);

		final CheckBox checkBoxIgnoreSoloMode = getContentSettingsContainer().getCheckboxPlaybackIgnoreSoloMode();
		final Label labelDescription = getContentSettingsContainer().getLabelIgnoreSoloModeDescription();

		assertThat(checkBoxIgnoreSoloMode.isDisabled()).isTrue();
		assertThat(labelDescription.isDisabled()).isTrue();

		// the option is not applicable in multi mode but the existing value must not be discarded
		assertThat(checkBoxIgnoreSoloMode.isSelected()).isTrue();

		robot.clickOn(checkBoxIgnoreSoloMode);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(checkBoxIgnoreSoloMode.isSelected()).isTrue();
	}

	@Test
	void testIgnoreSoloModeSettingsEnabledInSoloMode(FxRobot robot)
	{
		openPadSettings(createPad(false), true);

		final CheckBox checkBoxIgnoreSoloMode = getContentSettingsContainer().getCheckboxPlaybackIgnoreSoloMode();
		final Label labelDescription = getContentSettingsContainer().getLabelIgnoreSoloModeDescription();

		assertThat(checkBoxIgnoreSoloMode.isDisabled()).isFalse();
		assertThat(labelDescription.isDisabled()).isFalse();
		assertThat(checkBoxIgnoreSoloMode.isSelected()).isFalse();

		robot.clickOn(checkBoxIgnoreSoloMode);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(checkBoxIgnoreSoloMode.isSelected()).isTrue();
	}

	private AudioPadContentSettingsContainer getContentSettingsContainer()
	{
		return (AudioPadContentSettingsContainer) padSettingsGeneralViewController.getPadContentSettingsContainer();
	}

	private Pad createPad(boolean isIgnoreSoloMode)
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
						.isIgnoreSoloMode(isIgnoreSoloMode)
						.build())
				.build();
	}

	private void openPadSettings(Pad pad, boolean isSoloMode)
	{
		final Project project = loadProject("projects/project_1.json");
		project.getMetadata().setIsSoloMode(isSoloMode);
		context.get(ClientProjectController.class).loadProject(project);

		Platform.runLater(() -> {
			context.registerLazy(Stage.class, _ -> new Stage());
			PadSettingsViewController padSettingsViewController = context.get(PadSettingsViewController.class);
			padSettingsGeneralViewController = (PadSettingsGeneralViewController) padSettingsViewController.selectCategory(0);
			padSettingsViewController.showAndWait(new BasePadSettingsViewController.Param(pad, padSettingsViewController), stage);
		});

		WaitForAsyncUtils.waitForFxEvents();
	}
}
