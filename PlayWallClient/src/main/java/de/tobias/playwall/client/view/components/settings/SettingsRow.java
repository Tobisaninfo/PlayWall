package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

@DefaultProperty("content")
public class SettingsRow extends HBox
{
	private final Label labelName;
	private final FontIcon fontIcon;

	private final StringProperty title = new SimpleStringProperty();
	private final ObjectProperty<FontAwesomeType> icon = new SimpleObjectProperty<>();

	public SettingsRow()
	{
		labelName = new Label();
		labelName.setMinWidth(200);
		labelName.setPrefWidth(200);
		labelName.getStyleClass().add("settings-entry-label");

		fontIcon = icon.get() != null ? new FontIcon(icon.get()) : new FontIcon();
		fontIcon.setMouseTransparent(true);
		fontIcon.setSize(16);

		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);

		getChildren().addAll(fontIcon, labelName);

		labelName.textProperty().bind(title);
		icon.addListener((_, _, newIcon) -> {
			fontIcon.setIcons(newIcon);
		});
	}

	public StringProperty titleProperty()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title.set(title);
	}

	public String getTitle()
	{
		return title.get();
	}

	public FontAwesomeType getIcon()
	{
		return icon.get();
	}

	public void setIcon(FontAwesomeType icon)
	{
		this.icon.set(icon);
	}

	public ObjectProperty<FontAwesomeType> iconProperty()
	{
		return icon;
	}

	public ObservableList<Node> getContent()
	{
		return getChildren();
	}
}
