package de.tobias.playwall.project;

import java.nio.file.Path;
import java.util.Set;

public interface ProjectReference
{
	String FILE_EXTENSION = ".xml";

	String getName();

	Path getProjectPath();

	Set<Module> getMissedModules();
}
