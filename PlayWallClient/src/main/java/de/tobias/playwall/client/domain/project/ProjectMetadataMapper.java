package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class ProjectMetadataMapper
{
	private final ColorMapper colorMapper;

	public ProjectMetadata projectMetadataDtoToProjectMetadata(ProjectMetadataDto metadataDto)
	{
		return new ProjectMetadata(
				metadataDto.id(),
				metadataDto.name(),
				metadataDto.numberOfHorizontalPads(),
				metadataDto.numberOfVerticalPads(),
				metadataDto.volume(),
				metadataDto.timeMode(),
				colorMapper.colorToModernColor(metadataDto.defaultColor()),
				colorMapper.colorToModernColor(metadataDto.playColor()),
				metadataDto.eofWarningTime()
		);
	}

	public ProjectMetadataDto projectMetadataToProjectMetadataDto(ProjectMetadata metadata)
	{
		return new ProjectMetadataDto(
				metadata.getId(),
				metadata.getName(),
				metadata.getNumberOfHorizontalPads(),
				metadata.getNumberOfVerticalPads(),
				metadata.getVolume(),
				metadata.getTimeMode(),
				colorMapper.modernColorToColor(metadata.getDefaultColor()),
				colorMapper.modernColorToColor(metadata.getPlayColor()),
				metadata.getEofWarningTime()
		);
	}
}
