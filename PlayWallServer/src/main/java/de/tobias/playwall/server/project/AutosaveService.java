package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.api.settings.SettingsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
class AutosaveService
{
	private final TaskScheduler taskScheduler;
	private final ProjectController projectController;
	private final ProjectRepository projectRepository;
	private final AllProjectsInfoRepository allProjectsInfoRepository;
	private final SettingsService settingsService;
	private final ProjectSaveLock projectSaveLock;

	@Value("${de.tobias.playwall.autosave.cron:0 */5 * * * *}")
	private String cronExpression;

	private final AtomicReference<ScheduledFuture<?>> scheduledTask = new AtomicReference<>();

	@PostConstruct
	void init()
	{
		evaluateAndSchedule();
	}

	@EventListener
	void onSettingsUpdate(SettingsUpdate event)
	{
		evaluateAndSchedule();
	}

	private void evaluateAndSchedule()
	{
		final boolean shouldRun = settingsService.getSettings().isAutosave()
		                          && settingsService.getSettings().getUnsavedChangesMode() == UnsavedChangesMode.SAVE;

		if(shouldRun)
		{
			startAutosave();
		}
		else
		{
			stopAutosave();
		}
	}

	private void startAutosave()
	{
		log.info("Starting autosave with cron expression \"{}\"", cronExpression);
		final ScheduledFuture<?> newTask = taskScheduler.schedule(this::autosave, new CronTrigger(cronExpression));
		if(newTask != null && !scheduledTask.compareAndSet(null, newTask))
		{
			newTask.cancel(false);
		}
	}

	private void stopAutosave()
	{
		log.info("Stopping autosave");
		final ScheduledFuture<?> task = scheduledTask.getAndSet(null);
		if(task != null)
		{
			task.cancel(false);
		}
	}

	private void autosave()
	{
		if(!projectController.isAnyProjectLoaded())
		{
			return;
		}

		try
		{
			projectSaveLock.executeSave(() -> {
				try
				{
					projectRepository.saveProject(projectController.getLoadedProject());
					allProjectsInfoRepository.saveAllProjectsInfo();
					log.debug("Autosave completed");
				}
				catch(Exception e)
				{
					log.warn("Autosave failed", e);
				}
			});
		}
		catch(Exception e)
		{
			log.warn("Autosave failed", e);
		}
	}
}
