package de.tobias.playwall.common.api.project.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class")
public abstract sealed class PadContentDto permits AudioPadContentDto
{
}

