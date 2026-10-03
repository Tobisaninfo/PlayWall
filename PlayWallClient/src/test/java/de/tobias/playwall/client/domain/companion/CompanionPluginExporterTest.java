package de.tobias.playwall.client.domain.companion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanionPluginExporterTest
{
	@TempDir
	Path tempDir;

	private CompanionPluginExporter createExporter(boolean withPlugin) throws IOException
	{
		final Path resources = tempDir.resolve("resources");
		if(withPlugin)
		{
			final Path plugin = resources.resolve(CompanionPluginExporter.RESOURCE_PATH);
			Files.createDirectories(plugin.getParent());
			Files.writeString(plugin, "plugin-content");
		}
		else
		{
			Files.createDirectories(resources);
		}
		final ClassLoader classLoader = new URLClassLoader(new URL[]{resources.toUri().toURL()}, null);
		return new CompanionPluginExporter(classLoader);
	}

	@Test
	void export_copiesPluginAndReplacesExistingFile() throws IOException
	{
		final CompanionPluginExporter exporter = createExporter(true);
		final Path target = Files.createDirectories(tempDir.resolve("target"));
		Files.writeString(target.resolve(CompanionPluginExporter.FILE_NAME), "old");

		final Path exported = exporter.export(target);

		assertThat(exporter.isPluginAvailable()).isTrue();
		assertThat(exported).isEqualTo(target.resolve(CompanionPluginExporter.FILE_NAME));
		assertThat(Files.readString(exported, StandardCharsets.UTF_8)).isEqualTo("plugin-content");
	}

	@Test
	void export_throwsIfPluginIsNotBundled() throws IOException
	{
		final CompanionPluginExporter exporter = createExporter(false);

		assertThat(exporter.isPluginAvailable()).isFalse();
		assertThatThrownBy(() -> exporter.export(tempDir)).isInstanceOf(IOException.class);
	}
}
