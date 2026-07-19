package de.tobias.playwall.server.project;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
public class ProjectSaveLock
{
	private final ReentrantLock lock = new ReentrantLock();

	public void executeSave(Runnable saveAction)
	{
		lock.lock();
		try
		{
			saveAction.run();
		}
		finally
		{
			lock.unlock();
		}
	}
}
