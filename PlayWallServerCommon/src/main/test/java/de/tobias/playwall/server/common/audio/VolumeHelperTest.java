package de.tobias.playwall.server.common.audio;

import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
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
		assertThatThrownBy(() -> VolumeHelper.validateVolume(1.40)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testValidateVolumeExactlyMin()
	{
		assertDoesNotThrow(() -> VolumeHelper.validateVolume(0));
	}

	@Test
	void testValidateVolumeExactlyMax()
	{
		assertDoesNotThrow(() -> VolumeHelper.validateVolume(1.25));
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
		assertThat(VolumeHelper.convertVolumeToLogarithmic(1.25)).isCloseTo(1.56, Offset.offset(0.01));
	}

	@Test
	void testConvertVolumeToLogarithmicDefault()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(1.0)).isCloseTo(1.0, Offset.offset(0.01));
	}

	@Test
	void testConvertVolumeToLogarithmic50percent()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(0.5)).isCloseTo(0.25, Offset.offset(0.01));
	}

	@Test
	void testConvertVolumeToLogarithmic20percent()
	{
		assertThat(VolumeHelper.convertVolumeToLogarithmic(0.2)).isCloseTo(0.04, Offset.offset(0.01));
	}

	@Test
	void testCalculateVolume()
	{
		assertThat(VolumeHelper.calculateVolume(0.5, 0.25)).isEqualTo(0.125, Offset.offset(0.01));
	}

	@Test
	void testCalculateVolumeFromProject()
	{
		final ProjectMetadata projectMetadata = ProjectMetadata.builder()
				.volume(0.5)
				.build();

		final Project project = Project.builder()
				.metadata(projectMetadata)
				.build();

		assertThat(VolumeHelper.calculateVolume(project, 0.25)).isEqualTo(0.125, Offset.offset(0.01));
	}
}
