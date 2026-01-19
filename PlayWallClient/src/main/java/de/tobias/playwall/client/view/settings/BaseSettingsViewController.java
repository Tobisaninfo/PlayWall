package de.tobias.playwall.client.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.ParamViewControllerBase;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Base class for a page in a settings dialog.
 */
public abstract class BaseSettingsViewController<P> extends ParamViewControllerBase<P> implements Configurable<P>
{
	@FXML
	@Getter
	protected SettingsPage settingsPage;

	@Getter(AccessLevel.NONE)
	protected final FluentClient client;

	@Getter
	protected final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty();

	@InjectConstructor
	protected BaseSettingsViewController(FluentClient client)
	{
		this.client = client;
	}
}
