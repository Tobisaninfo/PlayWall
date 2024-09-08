package de.tobias.playwall.client.project;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public class ProjectReferenceMock implements ProjectReference
{
	private final UUID uuid;
	private final String name;

	public ProjectReferenceMock(UUID uuid, String name)
	{
		this.uuid = uuid;
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String getFileName()
	{
		return uuid + ProjectReference.FILE_EXTENSION;
	}

	@Override
	public Path getProjectPath()
	{
		App application = ApplicationUtils.getApplication();
		return application.getPath(PathType.DOCUMENTS, getFileName());
	}

	@Override
	public Set<Module> getMissedModules()
	{
		return Set.of();
	}

	public UUID getUuid()
	{
		return uuid;
	}
}
