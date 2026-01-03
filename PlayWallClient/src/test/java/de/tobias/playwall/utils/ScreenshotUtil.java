package de.tobias.playwall.utils;

import de.thecodelabs.logger.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.testfx.api.FxRobot;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreenshotUtil
{
	public static void takeScreenshot(Node node, String filename)
	{
		try
		{
			final FxRobot robot = new FxRobot();
			final Image image = robot.capture(node).getImage();

			final Path path = Paths.get(filename);
			if(Files.notExists(path.getParent()))
			{
				Files.createDirectories(path.getParent());
			}

			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", path.toFile());
			System.out.println("Screenshot saved to: " + filename);
		}
		catch(IOException e)
		{
			Logger.error(e);
		}
	}
}
