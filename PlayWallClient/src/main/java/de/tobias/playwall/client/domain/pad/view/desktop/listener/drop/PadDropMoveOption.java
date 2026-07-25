package de.tobias.playwall.client.domain.pad.view.desktop.listener.drop;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadIndex;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.PadDragListener;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.drag.DropOption;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PadDropMoveOption implements DropOption
{
	private final ClientProjectController projectController;
	private final FluentClient client;
	private final ErrorAlertBuilder errorAlertBuilder;

	@Override
	public void handleDrag(DesktopPadView padView, DragEvent event)
	{
		final Dragboard dragboard = event.getDragboard();
		final PadIndex sourcePadIndex = (PadIndex) dragboard.getContent(PadDragListener.DATA_FORMAT);
		final Pad sourcePad = projectController.getProject().getPad(sourcePadIndex);

		final Pad targetPad = padView.getPadController().getPad();

		try
		{
			client.currentProject().move(sourcePad.getId(), targetPad.getId());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot move pads", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_DRAG_MOVE), e.getMessage(), e.getError(), padView.getRootNode().getScene().getWindow()).showAndWait();
		}
	}

	@Override
	public String getLabel()
	{
		return Localization.getString(Strings.UI_PAD_DRAG_MOVE);
	}

	@Override
	public FontAwesomeType getIcon()
	{
		return FontAwesomeType.CIRCLE_ARROW_RIGHT_SOLID;
	}
}
