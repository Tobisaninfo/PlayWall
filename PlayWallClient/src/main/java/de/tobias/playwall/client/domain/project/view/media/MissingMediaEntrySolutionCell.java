package de.tobias.playwall.client.domain.project.view.media;

import de.tobias.playwall.client.view.components.PlayWallBadge;
import javafx.scene.control.TableCell;

class MissingMediaEntrySolutionCell extends TableCell<MissingMediaEntry, MissingMediaEntry>
{
	private final PlayWallBadge badge = new PlayWallBadge(null);

	@Override
	protected void updateItem(MissingMediaEntry item, boolean empty)
	{
		super.updateItem(item, empty);

		if(empty || item == null)
		{
			setGraphic(null);
			return;
		}

		badge.setOnAction(_ -> {
			item.setMissingMediaSolutionType(MissingMediaSolutionType.NONE);
			item.setNewMediaPath(null);
			getTableView().getItems().set(getIndex(), item);
		});

		final MissingMediaSolutionType solutionType = item.getMissingMediaSolutionType();
		badge.updateText(solutionType.getLocalizedName());
		badge.setVisible(true);

		switch(solutionType)
		{
			case NONE -> badge.setVisible(false);
			case REPLACE -> badge.getStyleClass().setAll("badge", "primary");
			case DELETE -> badge.getStyleClass().setAll("badge", "danger");
		}

		setGraphic(badge);
	}
}