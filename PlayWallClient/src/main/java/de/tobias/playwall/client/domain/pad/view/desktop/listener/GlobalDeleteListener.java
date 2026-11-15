package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class GlobalDeleteListener implements PadInputListener
{
	private final Consumer<Pad> onPadDeleted;

	@Override
	public void onMouseClicked(DesktopPadView padView, MouseEvent event)
	{
		if(event.getButton() == MouseButton.PRIMARY)
		{
			onPadViewClicked(padView);
			event.consume();
		}
	}

	private void onPadViewClicked(DesktopPadView padView)
	{
		final Pad pad = padView.getPadController().getPad();
		onPadDeleted.accept(pad);
	}
}
