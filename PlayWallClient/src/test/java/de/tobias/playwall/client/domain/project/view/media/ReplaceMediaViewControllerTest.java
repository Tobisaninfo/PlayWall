package de.tobias.playwall.client.domain.project.view.media;

import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReplaceMediaViewControllerTest
{
	@TempDir
	private Path tempDir;

	private ReplaceMediaViewController controller;

	@BeforeEach
	void setUp()
	{
		controller = new ReplaceMediaViewController(mock(FluentClient.class), mock(ErrorAlertBuilder.class), mock(FileChooserWrapper.class));
	}

	private MissingMediaEntry entry(String oldMediaPath)
	{
		return MissingMediaEntry.builder()
				.pageName("Page 1")
				.padId(UUID.randomUUID())
				.padPosition("1.1")
				.padName("Pad")
				.oldMediaPath(oldMediaPath)
				.build();
	}

	@Test
	void testExactFilenameMatchMarksEntryAsReplace() throws IOException
	{
		final Path foundFile = Files.createFile(tempDir.resolve("song.mp3"));
		final MissingMediaEntry entry = entry("/old/song.mp3");
		final List<MissingMediaEntry> entries = new ArrayList<>(List.of(entry));

		controller.matchMediaFiles(tempDir, entries);

		assertThat(entry.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.REPLACE);
		assertThat(entry.getNewMediaPath()).isEqualTo(foundFile.toString());
	}

	@Test
	void testFileInNestedSubdirectoryIsFound() throws IOException
	{
		final Path subDirectory = Files.createDirectories(tempDir.resolve("nested/deeper"));
		final Path foundFile = Files.createFile(subDirectory.resolve("clip.wav"));
		final MissingMediaEntry entry = entry("/old/clip.wav");
		final List<MissingMediaEntry> entries = new ArrayList<>(List.of(entry));

		controller.matchMediaFiles(tempDir, entries);

		assertThat(entry.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.REPLACE);
		assertThat(entry.getNewMediaPath()).isEqualTo(foundFile.toString());
	}

	@Test
	void testNonMatchingFilenamesStayUnchanged() throws IOException
	{
		Files.createFile(tempDir.resolve("other.mp3"));
		final MissingMediaEntry entry = entry("/old/missing.mp3");
		final List<MissingMediaEntry> entries = new ArrayList<>(List.of(entry));

		controller.matchMediaFiles(tempDir, entries);

		assertThat(entry.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.NONE);
		assertThat(entry.getNewMediaPath()).isNull();
	}

	@Test
	void testEntryWithoutOldMediaPathIsSkipped() throws IOException
	{
		Files.createFile(tempDir.resolve("song.mp3"));
		final MissingMediaEntry entry = entry(null);
		final List<MissingMediaEntry> entries = new ArrayList<>(List.of(entry));

		controller.matchMediaFiles(tempDir, entries);

		assertThat(entry.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.NONE);
		assertThat(entry.getNewMediaPath()).isNull();
	}

	@Test
	void testFilenameComparisonIsCaseInsensitive() throws IOException
	{
		final Path foundFile = Files.createFile(tempDir.resolve("Song.Mp3"));
		final MissingMediaEntry entry = entry("/old/song.mp3");
		final List<MissingMediaEntry> entries = new ArrayList<>(List.of(entry));

		controller.matchMediaFiles(tempDir, entries);

		assertThat(entry.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.REPLACE);
		assertThat(entry.getNewMediaPath()).isEqualTo(foundFile.toString());
	}

	@Test
	void testMultipleEntriesWithSameFilenameAreAllMatched() throws IOException
	{
		final Path foundFile = Files.createFile(tempDir.resolve("song.mp3"));
		final MissingMediaEntry entry1 = entry("/a/song.mp3");
		final MissingMediaEntry entry2 = entry("/b/song.mp3");
		final List<MissingMediaEntry> entries = new ArrayList<>(List.of(entry1, entry2));

		controller.matchMediaFiles(tempDir, entries);

		assertThat(entry1.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.REPLACE);
		assertThat(entry2.getMissingMediaSolutionType()).isEqualTo(MissingMediaSolutionType.REPLACE);
		assertThat(entry1.getNewMediaPath()).isEqualTo(foundFile.toString());
		assertThat(entry2.getNewMediaPath()).isEqualTo(foundFile.toString());
	}
}
