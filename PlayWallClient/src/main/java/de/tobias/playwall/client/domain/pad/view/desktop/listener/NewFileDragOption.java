package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.view.components.drag.DropOption;
import de.tobias.playwall.common.utils.FileFormats;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NewFileDragOption implements DropOption
{
	public static Optional<NewFileDragOption> create(Path path)
	{
		final Optional<FileFormats.PadContentType> contentType = FileFormats.tryContentTypeForFile(path);
		return contentType.map(NewFileDragOption::new);
	}

	private final FileFormats.PadContentType contentType;

	@Override
	public void handleDrag(DesktopPadView padView, DragEvent event)
	{
		final Dragboard dragboard = event.getDragboard();
		final List<File> files = dragboard.getFiles();
		final Path path = files.getFirst().toPath();
		padView.handleNewMediaPath(path);
	}

	@Override
	public String getLabel()
	{
		return Localization.getString("FileFormat." + contentType.name());
	}

	@Override
	public FontAwesomeType getIcon()
	{
		return switch(contentType)
		{
			case AUDIO -> FontAwesomeType.MUSIC_SOLID;
		};
	}
}
