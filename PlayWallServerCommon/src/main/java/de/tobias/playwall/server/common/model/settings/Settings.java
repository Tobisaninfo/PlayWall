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
			.autosave(false)
			.unsavedChangesMode(UnsavedChangesMode.ASK)
			.selectedAudioDevice(null)
			.build();

	@SuppressWarnings({"java:S116", "java:S1170"})
	private final int VERSION = 2;

	private boolean autoLoadLatestProjectOnStart;

	@Builder.Default
	private UnsavedChangesMode unsavedChangesMode = UnsavedChangesMode.ASK;

	private boolean autosave;

	private String selectedAudioDevice;
}
