package de.tobias.playwall.server.common.audio;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VolumeHelperTest
{
	@Test
	void testValidateVolumeLessThanMin()
	{
		assertThatThrownBy(() -> VolumeHelper.validateVolume(-0.5)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateVolumeGreaterThanMax()
	{
		assertThatThrownBy(() -> VolumeHelper.validateVolume(1.20)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateVolumeExactlyMin()
	{
		assertDoesNotThrow(() -> VolumeHelper.validateVolume(0));
	}

	@Test
	void testValidateVolumeExactlyMax()
	{
		assertDoesNotThrow(() -> VolumeHelper.validateVolume(1.15));
	}

	@Test
	void testValidateVolume()
	{
		assertDoesNotThrow(() -> VolumeHelper.validateVolume(0.75));
	}

	@Test
	void testConvertVolumeToLogarithmicMinValue()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(0.0)).isCloseTo(0, Offset.offset(0.01));
	}

	@Test
	void testConvertVolumeToLogarithmicMaxValue()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(1.15)).isCloseTo(1.41,  Offset.offset(0.01));
	}

	@Test
	void testConvertVolumeToLogarithmicDefault()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(1.0)).isCloseTo(0.54,  Offset.offset(0.01));
	}

	@Test
	void testConvertVolumeToLogarithmic50percent()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(0.5)).isCloseTo(0.025,  Offset.offset(0.01));
	}
}
