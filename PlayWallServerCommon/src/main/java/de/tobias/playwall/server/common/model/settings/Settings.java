package de.tobias.playwall.server.common.model.settings;

import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
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

	@Builder.Default
	private UnsavedChangesMode unsavedChangesMode =  UnsavedChangesMode.ASK;
}
