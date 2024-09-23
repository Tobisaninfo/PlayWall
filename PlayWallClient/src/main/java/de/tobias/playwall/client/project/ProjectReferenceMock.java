package de.tobias.playwall.client.project;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public class ProjectReferenceMock implements ProjectReference
{
	@Getter
	private final UUID id;
	private final String name;

	public ProjectReferenceMock(UUID uuid, String name)
	{
		this.id = uuid;
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public String getFileName()
	{
		return id + ProjectReference.FILE_EXTENSION;
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

}
