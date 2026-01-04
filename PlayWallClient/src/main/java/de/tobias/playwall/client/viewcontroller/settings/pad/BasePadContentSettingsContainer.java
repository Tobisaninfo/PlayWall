package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;

public abstract class BasePadContentSettingsContainer<T extends PadContent> extends VBox
{
	@Getter
	protected final SimpleBooleanProperty isValidProperty = new SimpleBooleanProperty();

	public BasePadContentSettingsContainer()
	{
		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);

		VBox.setVgrow(this, Priority.ALWAYS);
	}

	protected abstract void init(T padContent);

	public abstract void applySettings(T padContent);
}
