package de.tobias.playwall.server.config;

import de.tobias.playwall.server.storage.PathProvider;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@AllArgsConstructor
@Profile("!test")
public class ApplicationPidWriter {

	private final PathProvider provider;
	private final AtomicBoolean created = new AtomicBoolean(false);

	@EventListener
	public void onStarted(ApplicationStartedEvent event) {
		if (created.compareAndSet(false, true))
		{
			final File pidFile = provider.getPathFor("application.pid").toFile();
			try
			{
				new ApplicationPid().write(pidFile);
				pidFile.deleteOnExit();
			}
			catch(IOException e)
			{
				throw new IllegalStateException("Konnte PID-Datei nicht schreiben: " + pidFile, e);
			}
		}
	}
}