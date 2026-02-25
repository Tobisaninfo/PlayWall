package de.tobias.playwall.client.domain.settings;

import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public final class Settings
{
	private boolean autoLoadLatestProjectOnStart;
	private UnsavedChangesMode unsavedChangesMode;
}
