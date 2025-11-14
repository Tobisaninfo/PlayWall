package de.tobias.playwall.client;

import de.thecodelabs.utils.io.IOUtils;
import de.tobias.playwall.client.appcontext.Service;
import javafx.scene.image.Image;
import lombok.Getter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

@Getter
@Service
public class AppIconProvider
{
	private static final String ICON_PATH = "de/tobias/playwall/client/logo/icon_small.png";

	private final Image stageIcon;
	private final byte[] stageIconData;

	public AppIconProvider()
	{
		this.stageIcon = new Image(ICON_PATH);
		try {
			stageIconData = IOUtils.inputStreamToByteArray(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(ICON_PATH)));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
