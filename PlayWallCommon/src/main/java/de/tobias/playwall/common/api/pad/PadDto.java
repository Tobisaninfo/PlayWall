package de.tobias.playwall.common.api.pad;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.project.model.FadeSettingsDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PadDto
{
	private UUID id;
	private Integer position;
	private String name;
	private PadContentDto content;
	private TimeMode timeMode;
	private Color defaultColor;
	private Color playColor;
	private Color introColor;
	private Double eofWarningTime;
	private Double introDuration;
	private FadeSettingsDto fadeSettings;
}

