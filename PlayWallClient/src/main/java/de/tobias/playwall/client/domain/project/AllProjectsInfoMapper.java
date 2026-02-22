package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.project.model.AllProjectsInfoDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class AllProjectsInfoMapper
{
	private final ProjectMetadataMapper projectMetadataMapper;

	public AllProjectsInfo allProjectsInfoDtoToAllProjectsInfo(AllProjectsInfoDto allProjectsInfo)
	{
		return AllProjectsInfo.builder()
				.recentProjectIds(allProjectsInfo.recentProjectIds())
				.allProjectsMetadata(allProjectsInfo.allProjectsMetadata().stream().map(projectMetadataMapper::projectMetadataDtoToProjectMetadata).toList())
				.build();
	}

	public AllProjectsInfoDto allProjectsInfoToAllProjectsInfoDto(AllProjectsInfo allProjectsInfo)
	{
		return AllProjectsInfoDto.builder()
				.recentProjectIds(allProjectsInfo.getRecentProjectIds())
				.allProjectsMetadata(allProjectsInfo.getAllProjectsMetadata().stream().map(projectMetadataMapper::projectMetadataToProjectMetadataDto).toList())
				.build();
	}
}
