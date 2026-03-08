package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.AllProjectsInfoDto;
import de.tobias.playwall.server.common.model.project.AllProjectsInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProjectMetadataMapper.class})
public interface AllProjectsInfoMapper
{
	@Mapping(target = "recentProjectIds", expression = "java(allProjectsInfo.getRecentProjects().stream().toList())")
	AllProjectsInfoDto allProjectsInfoToAllProjectsInfoDto(AllProjectsInfo allProjectsInfo);
}
