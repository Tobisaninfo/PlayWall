package de.tobias.playwall.client.domain.pad.view.settings.content;

import de.tobias.playwall.client.domain.pad.PadContent;
import de.tobias.playwall.client.domain.pad.view.settings.PadSettingsGeneralViewController;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.settings.Configurable;
import de.tobias.playwall.client.domain.pad.view.settings.BasePadSettingsViewController;
import de.tobias.playwall.client.domain.pad.view.settings.PadSettingsViewController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.UUID;

/**
 * Base class for settings related to specific pad content types.
 * Subclasses of this class will be used to dynamically extend the settings in {@link PadSettingsGeneralViewController}.
 */
public abstract class BasePadContentSettingsContainer<T extends PadContent> extends VBox implements Configurable<BasePadSettingsViewController.Param>
{
	@Getter
	protected final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty();

	protected final T padContent;
	protected final UUID padId;
	protected final PadSettingsViewController parentDialog;

	protected BasePadContentSettingsContainer(T padContent, UUID padId, PadSettingsViewController parentDialog)
	{
		this.padContent = padContent;
		this.padId = padId;
		this.parentDialog = parentDialog;

		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);

		VBox.setVgrow(this, Priority.ALWAYS);
	}

	public abstract void applySettings(BasePadSettingsViewController.Param param);

	public abstract void cleanup();
}
