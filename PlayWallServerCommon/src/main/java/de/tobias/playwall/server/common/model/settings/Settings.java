package de.tobias.playwall.server.common.model.settings;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class Settings
{
	public static final Settings DEFAULT = Settings.builder()
			.autoLoadLatestProjectOnStart(false)
			.build();

	private final int VERSION = 1;

	private boolean autoLoadLatestProjectOnStart;
}
