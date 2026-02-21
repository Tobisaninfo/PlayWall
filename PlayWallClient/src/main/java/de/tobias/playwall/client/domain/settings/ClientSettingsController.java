package de.tobias.playwall.client.domain.settings;

import de.tobias.playwall.client.appcontext.Service;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class ClientSettingsController
{
	private Settings settings;
}
