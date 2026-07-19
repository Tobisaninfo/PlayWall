package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.api.settings.SettingsService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.model.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutosaveServiceTest
{
	@Mock
	private TaskScheduler taskScheduler;

	@Mock
	private ProjectController projectController;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@Mock
	private SettingsService settingsService;

	@SuppressWarnings("unchecked")
	private final ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);

	private final ProjectSaveLock projectSaveLock = new ProjectSaveLock();

	private AutosaveService service;

	@BeforeEach
	void setUp() throws Exception
	{
		service = new AutosaveService(taskScheduler, projectController, projectRepository, allProjectsInfoRepository, settingsService, projectSaveLock);

		final Field cronField = AutosaveService.class.getDeclaredField("cronExpression");
		cronField.setAccessible(true);
		cronField.set(service, "0 */5 * * * *");
	}

	@Test
	void testAutosaveNotStartedWhenDisabled()
	{
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(false)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.init();

		verify(taskScheduler, never()).schedule(any(), any(Trigger.class));
	}

	@Test
	void testAutosaveNotStartedWhenModeNotSave()
	{
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.ASK)
				.build());

		service.init();

		verify(taskScheduler, never()).schedule(any(), any(Trigger.class));
	}

	@Test
	void testAutosaveStartedWhenEnabledAndModeSave()
	{
		doReturn(mockFuture).when(taskScheduler).schedule(any(), any(Trigger.class));
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.init();

		verify(taskScheduler).schedule(any(), any(Trigger.class));
	}

	@Test
	void testAutosaveStoppedWhenSettingsChangedToDisabled()
	{
		doReturn(mockFuture).when(taskScheduler).schedule(any(), any(Trigger.class));
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.init();
		verify(taskScheduler).schedule(any(), any(Trigger.class));

		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(false)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.onSettingsUpdate(new SettingsUpdate(SettingsDto.builder()
				.autosave(false)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build()));

		verify(mockFuture).cancel(false);
	}

	@Test
	void testAutosaveSkipsWhenNoProjectLoaded() throws IOException
	{
		doAnswer(invocation -> {
			final Runnable runnable = invocation.getArgument(0);
			runnable.run();
			return mockFuture;
		}).when(taskScheduler).schedule(any(), any(Trigger.class));

		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());
		when(projectController.isAnyProjectLoaded()).thenReturn(false);

		service.init();

		verify(projectRepository, never()).saveProject(any());
	}

	@Test
	void testAutosaveSavesWhenProjectLoaded() throws IOException
	{
		final Project project = buildProject();

		final ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
		doReturn(mockFuture).when(taskScheduler).schedule(runnableCaptor.capture(), any(Trigger.class));
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.init();

		when(projectController.isAnyProjectLoaded()).thenReturn(true);
		when(projectController.getLoadedProject()).thenReturn(project);

		runnableCaptor.getValue().run();

		verify(projectRepository).saveProject(project);
		verify(allProjectsInfoRepository).saveAllProjectsInfo();
	}

	@Test
	void testConcurrentStartDoesNotDuplicateTask()
	{
		doReturn(mockFuture).when(taskScheduler).schedule(any(), any(Trigger.class));
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.onSettingsUpdate(new SettingsUpdate(SettingsDto.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build()));
		service.onSettingsUpdate(new SettingsUpdate(SettingsDto.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build()));

		verify(taskScheduler, times(2)).schedule(any(), any(Trigger.class));
		verify(mockFuture, times(1)).cancel(false);
	}

	@Test
	void testAutosaveRestartAfterStop()
	{
		doReturn(mockFuture).when(taskScheduler).schedule(any(), any(Trigger.class));
		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		service.init();
		verify(taskScheduler).schedule(any(), any(Trigger.class));

		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(false)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());
		service.onSettingsUpdate(new SettingsUpdate(SettingsDto.builder()
				.autosave(false)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build()));
		verify(mockFuture).cancel(false);

		when(settingsService.getSettings()).thenReturn(Settings.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());
		service.onSettingsUpdate(new SettingsUpdate(SettingsDto.builder()
				.autosave(true)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build()));
		verify(taskScheduler, times(2)).schedule(any(), any(Trigger.class));
	}

	private Project buildProject()
	{
		return Project.builder()
				.metadata(ProjectMetadata.builder()
						.id(UUID.randomUUID())
						.name("Test")
						.numberOfHorizontalPads(2)
						.numberOfVerticalPads(2)
						.build())
				.pages(List.of())
				.build();
	}
}
