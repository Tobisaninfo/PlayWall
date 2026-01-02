package de.tobias.playwall.client.viewcontroller.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.viewcontroller.ParamView;
import de.tobias.playwall.client.viewcontroller.ViewControllerBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class BaseSettingsViewController<P> extends ViewControllerBase implements ParamView<P>
{
	@FXML
	@Getter
	protected SettingsPage settingsPage;

	@Getter(AccessLevel.NONE)
	protected final FluentClient client;

	@Getter
	protected final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty();

	@InjectConstructor
	public BaseSettingsViewController(FluentClient client)
	{
		this.client = client;
	}

	public abstract void initParameter(P param);

	public abstract void applySettings(P param);
}
