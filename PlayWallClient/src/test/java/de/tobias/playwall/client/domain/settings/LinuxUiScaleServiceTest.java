package de.tobias.playwall.client.domain.settings;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinuxUiScaleServiceTest
{
	@Test
	void withUiScale_insertsOptionAfterLastJavaOptions()
	{
		final List<String> lines = List.of("[JavaOptions]", "java-options=-Xmx1g", "java-options=-Dfoo=bar", "[ArgOptions]");

		assertEquals(List.of("[JavaOptions]", "java-options=-Xmx1g", "java-options=-Dfoo=bar",
				"java-options=-Dglass.gtk.uiScale=150%", "[ArgOptions]"), LinuxUiScaleService.withUiScale(lines, 150));
	}

	@Test
	void withUiScale_replacesExistingOption()
	{
		final List<String> lines = List.of("java-options=-Dglass.gtk.uiScale=100%", "java-options=-Xmx1g");

		assertEquals(List.of("java-options=-Xmx1g", "java-options=-Dglass.gtk.uiScale=200%"),
				LinuxUiScaleService.withUiScale(lines, 200));
	}

	@Test
	void withUiScale_autoRemovesOption()
	{
		final List<String> lines = List.of("java-options=-Xmx1g", "java-options=-Dglass.gtk.uiScale=125%");

		assertEquals(List.of("java-options=-Xmx1g"), LinuxUiScaleService.withUiScale(lines, LinuxUiScaleService.UI_SCALE_AUTO));
		assertEquals(List.of("java-options=-Xmx1g"), LinuxUiScaleService.withUiScale(lines, null));
	}

	@Test
	void withUiScale_appendsWhenNoJavaOptionsExist()
	{
		assertEquals(List.of("[Application]", "java-options=-Dglass.gtk.uiScale=175%"),
				LinuxUiScaleService.withUiScale(List.of("[Application]"), 175));
	}

	@Test
	void withUiScale_doesNotModifyInput()
	{
		final List<String> lines = List.of("java-options=-Xmx1g");
		LinuxUiScaleService.withUiScale(lines, 125);
		assertEquals(List.of("java-options=-Xmx1g"), lines);
	}

	@Test
	void launcherConfigFileFor_resolvesNextToLauncher()
	{
		assertEquals(Optional.of(Path.of("/opt/PlayWall/lib/app/PlayWall.cfg")),
				LinuxUiScaleService.launcherConfigFileFor(Path.of("/opt/PlayWall/bin/PlayWall")));
	}

	@Test
	void launcherConfigFileFor_emptyWithoutAppRoot()
	{
		assertTrue(LinuxUiScaleService.launcherConfigFileFor(Path.of("PlayWall")).isEmpty());
	}

	@Test
	void appImageScaleFile_usesXdgConfigHomeWhenSet()
	{
		assertEquals(Path.of("/custom/PlayWall/appimage-ui-scale"),
				LinuxUiScaleService.getAppImageScaleFilePath("/custom", "/home/user"));
	}

	@Test
	void appImageScaleFile_fallsBackToHomeConfig()
	{
		final Path expected = Path.of("/home/user/.config/PlayWall/appimage-ui-scale");
		assertEquals(expected, LinuxUiScaleService.getAppImageScaleFilePath(null, "/home/user"));
		assertEquals(expected, LinuxUiScaleService.getAppImageScaleFilePath(" ", "/home/user"));
	}
}
