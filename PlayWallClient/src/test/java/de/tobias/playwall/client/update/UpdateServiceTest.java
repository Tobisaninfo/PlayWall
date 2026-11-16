package de.tobias.playwall.client.update;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateServiceTest
{
	@Test
	void isVersionNewer_isNewer() throws UpdateCheckException
	{
		assertThat(UpdateService.isVersionNewer("1.8.0", "1.9.0")).isTrue();
	}

	@Test
	void isVersionNewer_isOlder() throws UpdateCheckException
	{
		assertThat(UpdateService.isVersionNewer("1.9.0", "1.7.0")).isFalse();
	}

	@Test
	void isVersionNewer_isEqual() throws UpdateCheckException
	{
		assertThat(UpdateService.isVersionNewer("1.8.0", "1.8.0")).isFalse();
	}

	@Test
	void isVersionNewer_isEqualSnapshot() throws UpdateCheckException
	{
		assertThat(UpdateService.isVersionNewer("1.8.0", "1.8.0-SNAPSHOT")).isFalse();
	}

	@Test
	void isVersionNewer_withSnapshot() throws UpdateCheckException
	{
		assertThat(UpdateService.isVersionNewer("8.4.0-SNAPSHOT", "8.3.1")).isFalse();
	}

	@Test
	void isVersionNewer_invalid()
	{
		assertThatThrownBy(() -> UpdateService.isVersionNewer("A8DF4SSA", "UHZDSAujsva"))
				.isInstanceOf(UpdateCheckException.class);
	}
}
