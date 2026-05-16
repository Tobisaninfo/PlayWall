package de.tobias.playwall.client.domain.project.view.media;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.view.FileChooserWrapper;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

class MissingMediaEntryTableCell extends TableCell<MissingMediaEntry, MissingMediaEntry>
{
	private final Button buttonChoose = new Button(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH));
	private final Button buttonDelete = new Button(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE));
	private final HBox buttonBox = new HBox(5, buttonChoose, buttonDelete);

	public MissingMediaEntryTableCell()
	{
		buttonChoose.setOnAction(e -> {
			final MissingMediaEntry entry = getTableView().getItems().get(getIndex());

			final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
			fileChooser.showByActionEvent(e).ifPresent(path -> {
				entry.setMissingMediaSolutionType(MissingMediaSolutionType.REPLACE);
				entry.setNewMediaPath(path.toString());
			});

			getTableView().getItems().set(getIndex(), entry);
		});

		buttonDelete.setOnAction(e -> {
			final MissingMediaEntry entry = getTableView().getItems().get(getIndex());
			entry.setMissingMediaSolutionType(MissingMediaSolutionType.DELETE);
			entry.setNewMediaPath(null);
			getTableView().getItems().set(getIndex(), entry);
		});
	}

	@Override
	protected void updateItem(MissingMediaEntry item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(empty ? null : buttonBox);
	}
}