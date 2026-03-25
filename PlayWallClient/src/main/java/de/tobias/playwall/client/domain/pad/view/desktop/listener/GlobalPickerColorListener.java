package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.view.components.GlobalColorPicker;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

@AllArgsConstructor
public class GlobalPickerColorListener extends PadInputListener
{
	private final GlobalColorPicker colorPicker;
	private final BiConsumer<Pad, ModernColor> onColorChange;
	private final BiConsumer<Set<UUID>, ModernColor> onColorSubmit;

	private final Set<UUID> dragAndDropSelectedPads = new HashSet<>();

	@Override
	public void onMouseClicked(DesktopPadView padView, MouseEvent event)
	{
		if(event.getButton() == MouseButton.PRIMARY)
		{
			onPadViewSelected(padView);
			updatePadColor(dragAndDropSelectedPads);
			event.consume();
		}
	}

	@Override
	public void onDragDetected(DesktopPadView padView, MouseEvent event)
	{
		if(event.isPrimaryButtonDown())
		{
			padView.getRootNode().startFullDrag();
			onPadViewSelected(padView);
			event.consume();
		}
	}

	@Override
	public void onMouseDragEntered(DesktopPadView padView, MouseEvent event)
	{
		if(event.isPrimaryButtonDown())
		{
			onPadViewSelected(padView);
			event.consume();
		}
	}

	@Override
	public void onMouseReleased(DesktopPadView padView, MouseEvent event)
	{
		updatePadColor(dragAndDropSelectedPads);
		event.consume();
	}

	private void onPadViewSelected(DesktopPadView padView)
	{
		final Pad pad = padView.getPadController().getPad();
		dragAndDropSelectedPads.add(pad.getId());
		onColorChange.accept(pad, colorPicker.getSelectedColor());
	}

	private void updatePadColor(Set<UUID> padIds)
	{
		if(!padIds.isEmpty())
		{
			onColorSubmit.accept(padIds, colorPicker.getSelectedColor());
		}

		dragAndDropSelectedPads.clear();
	}
}
