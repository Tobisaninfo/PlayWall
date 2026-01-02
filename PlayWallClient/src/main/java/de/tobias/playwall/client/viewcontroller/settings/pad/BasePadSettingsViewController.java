package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.viewcontroller.ParamViewControllerBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class BasePadSettingsViewController extends ParamViewControllerBase<BasePadSettingsViewController.Param>
{
	@AllArgsConstructor
	public static class Param
	{
		protected Pad pad;
	}

	@FXML
	@Getter(AccessLevel.PACKAGE)
	protected SettingsPage settingsPage;

	@Getter(AccessLevel.NONE)
	protected final FluentClient client;

	@Getter
	protected final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty();

	@InjectConstructor
	public BasePadSettingsViewController(FluentClient client)
	{
		this.client = client;
	}

	public abstract void initParameter(Param param);

	public abstract void applySettings(Pad pad);
}
