package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.storage.PathProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@AllArgsConstructor
public class ProjectRepository
{
	private final PathProvider pathProvider;
	private final ObjectMapper mapper;

	public List<ProjectMetadataModel> getAllProjectMetadata() throws IOException
	{
		final Path path = pathProvider.getPathForConfig("projects.json");
		return mapper.readValue(Files.newBufferedReader(path), new TypeReference<>()
		{
		});
	}
}
