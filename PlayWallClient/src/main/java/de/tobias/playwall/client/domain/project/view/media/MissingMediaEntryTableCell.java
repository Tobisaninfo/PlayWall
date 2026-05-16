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

class MissingMediaEntryTableCell extends TableCell<MissingMediaEntry, MissingMediaEntry>
{
	final HBox box = new HBox();

	public MissingMediaEntryTableCell()
	{
		final PlayWallButton buttonChooseFile = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH), FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonChooseFile.setOnAction(e -> {
			final MissingMediaEntry entry = getTableView().getItems().get(getIndex());

			final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
			fileChooser.showByActionEvent(e).ifPresent(path -> {
				entry.setMissingMediaSolutionType(MissingMediaSolutionType.REPLACE);
				entry.setNewMediaPath(path.toString());
			});

			getTableView().getItems().set(getIndex(), entry);
		});

		final PlayWallButton buttonDelete = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE), FontAwesomeType.TRASH_CAN_SOLID);
		buttonDelete.getStyleClass().add("danger");
		buttonDelete.setOnAction(e -> {
			final MissingMediaEntry entry = getTableView().getItems().get(getIndex());
			entry.setMissingMediaSolutionType(MissingMediaSolutionType.DELETE);
			entry.setNewMediaPath(null);
			getTableView().getItems().set(getIndex(), entry);
		});

		box.setSpacing(ViewConstants.DEFAULT_SPACING / 2);
		box.getChildren().addAll(buttonChooseFile, buttonDelete);
		box.setAlignment(Pos.CENTER_LEFT);

		setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	protected void updateItem(MissingMediaEntry item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(empty ? null : box);
	}
}