package de.tobias.playwall.client.di;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppContextHolder
{
	@Getter
	@Setter
	private static AppContext instance;
}
