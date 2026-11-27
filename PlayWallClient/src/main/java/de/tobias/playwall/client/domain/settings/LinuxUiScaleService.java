package de.tobias.playwall.client.domain.settings;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.tobias.playwall.client.PlayWallMain.UI_SCALE_FILE_NAME;

/**
 * Persists the Linux UI scale ({@code glass.gtk.uiScale}) and makes sure it reaches the JVM at the next start.
 * <p>
 * Glass only picks up {@code glass.gtk.uiScale} when it is present as a real JVM argument at process launch, not via
 * {@link System#setProperty} from within an already-running JVM. How the argument gets there depends on how PlayWall
 * is installed:
 * <ul>
 *     <li><b>jpackage app image</b> (generic Linux tarball): the launch arguments come from the native launcher's
 *     {@code .cfg} file (e.g. {@code lib/app/PlayWall.cfg}), which is edited in place.</li>
 *     <li><b>AppImage</b>: the {@code .cfg} is on a read-only squashfs mount, so the scale is stored in a small file
 *     that {@code packaging/appimage/AppRun} reads before the JVM starts. Path and format must match AppRun.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor})
@Slf4j
public class LinuxUiScaleService
{
	public static final int UI_SCALE_AUTO = 0;

	private static final String CFG_JAVA_OPTIONS_PREFIX = "java-options=";
	private static final String CFG_UI_SCALE_PREFIX = CFG_JAVA_OPTIONS_PREFIX + "-Dglass.gtk.uiScale=";

	private final App app;

	/**
	 * @return the stored scale in percent, or {@link #UI_SCALE_AUTO}
	 */
	public Integer readScale()
	{
		final Path uiScaleFile = app.getPath(PathType.CONFIGURATION, UI_SCALE_FILE_NAME);
		if(Files.exists(uiScaleFile))
		{
			try
			{
				final String content = Files.readString(uiScaleFile).trim();
				if(!content.isEmpty())
				{
					return Integer.parseInt(content);
				}
			}
			catch(IOException | NumberFormatException e)
			{
				log.warn("Cannot read UI scale file", e);
			}
		}
		return UI_SCALE_AUTO;
	}

	/**
	 * Stores the scale and propagates it to the launcher, so it takes effect on the next start.
	 *
	 * @param scale scale in percent, {@code null} or {@link #UI_SCALE_AUTO} to use the automatic scale
	 */
	public void applyScale(Integer scale) throws IOException
	{
		final Path uiScaleFile = app.getPath(PathType.CONFIGURATION, UI_SCALE_FILE_NAME);
		if(isAuto(scale))
		{
			Files.deleteIfExists(uiScaleFile);
		}
		else
		{
			Files.writeString(uiScaleFile, String.valueOf(scale));
		}

		if(isAppImage())
		{
			updateAppImageScale(scale);
		}
		else
		{
			updateLauncherConfig(scale);
		}
	}

	// region AppImage

	private static boolean isAppImage()
	{
		return System.getenv("APPIMAGE") != null;
	}

	private static void updateAppImageScale(Integer scale) throws IOException
	{
		final Path scaleFile = getAppImageScaleFilePath(System.getenv("XDG_CONFIG_HOME"), System.getProperty("user.home"));
		if(isAuto(scale))
		{
			Files.deleteIfExists(scaleFile);
			return;
		}
		Files.createDirectories(scaleFile.getParent());
		Files.writeString(scaleFile, String.valueOf(scale));
		log.debug("Wrote ui scale option (AppImage) to file {}", scaleFile);
	}

	static Path getAppImageScaleFilePath(String xdgConfigHome, String userHome)
	{
		final Path configHome = xdgConfigHome != null && !xdgConfigHome.isBlank()
				? Path.of(xdgConfigHome)
				: Path.of(userHome, ".config");
		return configHome.resolve("PlayWall").resolve("appimage-ui-scale");
	}

	// endregion

	// region jpackage launcher config

	private void updateLauncherConfig(Integer scale) throws IOException
	{
		final Optional<Path> cfgFile = findLauncherConfigFile();
		if(cfgFile.isEmpty())
		{
			return;
		}
		if(!Files.isWritable(cfgFile.get()))
		{
			log.warn("Launcher config {} is not writable, UI scale will not be applied on next start", cfgFile.get());
			return;
		}

		final List<String> lines = Files.readAllLines(cfgFile.get());
		Files.write(cfgFile.get(), withUiScale(lines, scale));
		log.debug("Wrote ui scale option (jpackage) to file {}", cfgFile);
	}

	/**
	 * Locates the jpackage native launcher's {@code .cfg} file next to the currently running launcher binary.
	 * Returns empty when not running from a jpackage app image (e.g. a dev run), in which case there is nothing
	 * to update.
	 */
	private static Optional<Path> findLauncherConfigFile()
	{
		return ProcessHandle.current().info().command()
				.map(Path::of)
				.flatMap(LinuxUiScaleService::launcherConfigFileFor)
				.filter(Files::isRegularFile);
	}

	/**
	 * {@code <AppRoot>/bin/PlayWall} -> {@code <AppRoot>/lib/app/PlayWall.cfg}
	 */
	static Optional<Path> launcherConfigFileFor(Path launcher)
	{
		final Path binDir = launcher.getParent();
		final Path appRoot = binDir == null ? null : binDir.getParent();
		if(appRoot == null)
		{
			return Optional.empty();
		}
		return Optional.of(appRoot.resolve("lib").resolve("app").resolve(launcher.getFileName() + ".cfg"));
	}

	/**
	 * Returns a copy of the launcher config lines with the UI scale option replaced: any existing option is removed
	 * and, unless the scale is automatic, a new one is inserted after the last {@code java-options=} line.
	 */
	static List<String> withUiScale(List<String> lines, Integer scale)
	{
		final List<String> result = new ArrayList<>(lines);
		int lastJavaOptionsIndex = -1;
		for(int i = 0; i < result.size(); i++)
		{
			final String line = result.get(i);
			if(line.startsWith(CFG_UI_SCALE_PREFIX))
			{
				result.remove(i);
				i--;
				continue;
			}
			if(line.startsWith(CFG_JAVA_OPTIONS_PREFIX))
			{
				lastJavaOptionsIndex = i;
			}
		}

		if(!isAuto(scale))
		{
			final String newLine = CFG_UI_SCALE_PREFIX + scale + "%";
			if(lastJavaOptionsIndex >= 0)
			{
				result.add(lastJavaOptionsIndex + 1, newLine);
			}
			else
			{
				result.add(newLine);
			}
		}
		return result;
	}

	// endregion

	private static boolean isAuto(Integer scale)
	{
		return scale == null || scale == UI_SCALE_AUTO;
	}
}
