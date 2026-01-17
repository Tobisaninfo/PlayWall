package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.utils.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientProjectControllerTest extends AbstractTest
{
	private Project project;

	private ClientProjectController controller;

	@BeforeEach
	void init()
	{
		project = loadProject("projects/project_1.json");
		controller = AppContextHolder.getInstance().get(ClientProjectController.class);
		controller.loadProject(project);
	}

	@Test
	void testUpdatePad()
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		controller.updatePad(Pad.builder()
				.name("Lorem")
				.id(padId)
				.position(0)
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.volume(1.0)
						.build())
				.build());

		assertThat(project.getPad(padId))
				.satisfies(pad -> assertThat(pad.getId()).isEqualTo(padId))
				.satisfies(pad -> assertThat(pad.getPosition()).isZero())
				.satisfies(pad -> assertThat(pad.getName()).isEqualTo("Lorem"))
				.satisfies(pad -> assertThat(pad.getContent()).isInstanceOf(AudioPadContent.class));
	}

	@Test
	void testUpdatePadNotFound()
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb65793d616");
		controller.updatePad(Pad.builder()
				.name("Lorem")
				.id(padId)
				.position(0)
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.volume(1.0)
						.build())
				.build());

		assertThat(project.getPad(padId)).isNull();
	}

}
