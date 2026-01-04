package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.Getter;

@DefaultProperty("content")
public class SettingsRow extends GridPane
{
	private final Label labelName;
	private final FontIcon fontIcon;

	private final StringProperty title = new SimpleStringProperty();
	private final ObjectProperty<FontAwesomeType> icon = new SimpleObjectProperty<>();

	@Getter
	private final ObservableList<Node> content = FXCollections.observableArrayList();

	public SettingsRow()
	{
		labelName = new Label();
		labelName.setMinWidth(150);
		labelName.setPrefWidth(150);
		labelName.getStyleClass().add("settings-entry-label");

		fontIcon = icon.get() != null ? new FontIcon(icon.get()) : new FontIcon();
		fontIcon.setMouseTransparent(true);
		fontIcon.setSize(16);

		final HBox leftBox = new HBox(ViewConstants.DEFAULT_SPACING, fontIcon, labelName);
		leftBox.setAlignment(Pos.CENTER_LEFT);

		setVgap(ViewConstants.DEFAULT_SPACING);
		setHgap(ViewConstants.DEFAULT_SPACING);
		GridPane.setValignment(leftBox, VPos.TOP);

		add(leftBox, 0, 0);

		labelName.textProperty().bind(title);
		icon.addListener((_, _, newIcon) ->
				fontIcon.setIcons(newIcon));

		content.addListener((InvalidationListener) _ -> {
			getChildren().removeIf(node -> {
				Integer columnIndex = GridPane.getColumnIndex(node);
				return columnIndex != null && columnIndex == 1;
			});
			for(int i = 0; i < content.size(); i++)
			{
				add(content.get(i), 1, i);
			}
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
}
