package de.tobias.playwall.client.domain.pad.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.common.FadeSettingsController;
import de.tobias.playwall.client.domain.pad.view.settings.content.PadContentSettingsContainerFactory;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Viewcontroller for the view page in the pad settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsFadePageView", applyToStage = false)
@Getter
public class PadSettingsFadeViewController extends BasePadSettingsViewController
{
	@FXML
	private CheckBox enableCheckbox;
	@FXML
	private FadeSettingsController fadeSettingsController;

	@Getter(AccessLevel.NONE)
	private final ClientProjectController projectController;

	@InjectConstructor
	public PadSettingsFadeViewController(FluentClient client, PadContentSettingsContainerFactory padContentSettingsContainerFactory, ClientProjectController projectController)
	{
		super(client);
		this.projectController = projectController;
	}

	@Override
	public void initParameter(Param param)
	{
		fadeSettingsController.getRoot().setDisable(param.getPad().getFadeSettings() == null);
		enableCheckbox.setSelected(param.getPad().getFadeSettings() != null);

		if(param.getPad().getFadeSettings() == null)
		{
			fadeSettingsController.initParameter(projectController.getProject().getMetadata().getFadeSettings());
		}
		else
		{
			fadeSettingsController.initParameter(param.getPad().getFadeSettings());
		}

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		if(enableCheckbox.isSelected())
		{
			final FadeSettings fadeSettings = new FadeSettings();
			fadeSettingsController.applySettings(fadeSettings);
			param.getPad().setFadeSettings(fadeSettings);
		}
		else
		{
			param.getPad().setFadeSettings(null);
		}
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}

	@FXML
	private void onEnableCheckbox()
	{
		final boolean isSelected = enableCheckbox.isSelected();
		fadeSettingsController.getRoot().setDisable(!isSelected);
	}
}
