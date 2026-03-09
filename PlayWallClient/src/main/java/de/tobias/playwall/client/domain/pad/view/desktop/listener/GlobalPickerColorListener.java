package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.view.components.GlobalColorPicker;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.AllArgsConstructor;

import java.util.function.BiConsumer;

@AllArgsConstructor
public class GlobalPickerColorListener implements PadInputListener
{

	private final GlobalColorPicker colorPicker;
	private final BiConsumer<Pad, ModernColor> onColorChange;

	@Override
	public void onMouseClicked(DesktopPadView padView, MouseEvent event)
	{
		if(event.getButton() == MouseButton.PRIMARY)
		{
			updatePadColor(padView);
			event.consume();
		}
	}

	@Override
	public void onDragDetected(DesktopPadView padView, MouseEvent event)
	{
		if(event.isPrimaryButtonDown())
		{
			padView.getRootNode().startFullDrag();
			updatePadColor(padView);
			event.consume();
		}
	}

	@Override
	public void onMouseDragEntered(DesktopPadView padView, MouseEvent event)
	{
		if(event.isPrimaryButtonDown())
		{
			updatePadColor(padView);
			event.consume();
		}
	}

	private void updatePadColor(PadView padView)
	{
		final Pad pad = padView.getPadController().getPad();
		if(pad.getDefaultColor() == colorPicker.getSelectedColor())
		{
			return;
		}

		onColorChange.accept(pad, colorPicker.getSelectedColor());
	}
}
