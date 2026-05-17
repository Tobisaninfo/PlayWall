package de.tobias.playwall.client.domain.project.view.media;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

class MissingMediaEntryActionCell extends TableCell<MissingMediaEntry, MissingMediaEntry>
{
	private final HBox box = new HBox();
	private final PlayWallButton buttonChooseFile;
	private final PlayWallButton buttonDelete;

	public MissingMediaEntryActionCell()
	{
		buttonChooseFile = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH), FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonDelete = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE), FontAwesomeType.TRASH_CAN_SOLID);
		buttonDelete.getStyleClass().add("danger");

		box.setSpacing(ViewConstants.DEFAULT_SPACING / 2);
		box.getChildren().addAll(buttonChooseFile, buttonDelete);
		box.setAlignment(Pos.CENTER_LEFT);

		setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	protected void updateItem(MissingMediaEntry item, boolean empty)
	{
		super.updateItem(item, empty);

		if(empty || item == null)
		{
			setGraphic(null);
			return;
		}

		buttonChooseFile.setOnAction(e -> {
			final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
			fileChooser.showByActionEvent(e).ifPresent(path -> {
				item.setMissingMediaSolutionType(MissingMediaSolutionType.REPLACE);
				item.setNewMediaPath(path.toString());
			});

			getTableView().getItems().set(getIndex(), item);
		});

		buttonDelete.setOnAction(_ -> {
			item.setMissingMediaSolutionType(MissingMediaSolutionType.DELETE);
			item.setNewMediaPath(null);
			getTableView().getItems().set(getIndex(), item);
		});

		setGraphic(box);
	}
}