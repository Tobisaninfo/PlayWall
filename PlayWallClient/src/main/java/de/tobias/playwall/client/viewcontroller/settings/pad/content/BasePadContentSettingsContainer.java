package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.viewcontroller.settings.pad.BasePadSettingsViewController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.UUID;

/**
 * Base class for settings related to specific pad content types.
 * Subclasses of this class will be used to dynamically extend the settings in {@link de.tobias.playwall.client.viewcontroller.settings.pad.PadSettingsGeneralViewController}.
 */
public abstract class BasePadContentSettingsContainer<T extends PadContent> extends VBox
{
	@Getter
	protected final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty();

	protected final T padContent;
	protected final UUID padId;

	protected BasePadContentSettingsContainer(T padContent, UUID padId)
	{
		this.padContent = padContent;
		this.padId = padId;

		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);

		VBox.setVgrow(this, Priority.ALWAYS);
	}

	public abstract void applySettings(BasePadSettingsViewController.Param param);

	public abstract void cleanup();
}
