package de.tobias.playwall.client;

import de.tobias.playwall.client.appcontext.Service;
import javafx.scene.text.Font;
import lombok.Getter;

import java.net.URL;
import java.util.Objects;

@Getter
@Service
public class FontLoader
{
	public FontLoader()
	{
		final URL resource = this.getClass().getClassLoader().getResource("fonts/fontawesome-webfont.ttf");
		Font.loadFont(Objects.requireNonNull(resource).toExternalForm(), 16);
	}
}
