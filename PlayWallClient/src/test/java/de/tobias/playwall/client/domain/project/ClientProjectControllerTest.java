package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.utils.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

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
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(ModernColor.LIGHT_GREEN1)
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
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(ModernColor.LIGHT_GREEN1)
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.volume(1.0)
						.build())
				.build());

		assertThat(project.getPad(padId)).isNull();
	}

	@Test
	void testAddPage()
	{
		final Pad pad1 = new Pad(UUID.randomUUID(), 0, null, null, null, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null, null, null);
		final Pad pad2 = new Pad(UUID.randomUUID(), 0, null, null, null, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null, null, null);
		final Page newPage = new Page(UUID.randomUUID(), new PageSettings("Seite 3", ModernColor.GRAY1), 2, List.of(pad1, pad2));

		controller.addPage(newPage);

		assertThat(project.getPage(newPage.getPosition())).isEqualTo(newPage);
		assertThat(controller.getPadController(pad1.getId())).isNotNull()
				.satisfies(padController -> assertThat(padController.getPad()).isEqualTo(pad1));
		assertThat(controller.getPadController(pad2.getId())).isNotNull()
				.satisfies(padController -> assertThat(padController.getPad()).isEqualTo(pad2));
	}

	@Test
	void testAsAtLeastOnePadPlayingFalse()
	{
		assertThat(controller.isAtLeastOnePadPlaying()).isFalse();
	}

	@Test
	void testAsAtLeastOnePadPlayingTrue()
	{
		final UUID padId = UUID.fromString("46ea0972-f5b4-433a-8841-19ccf7aa6f31");
		final ClientPadController padController = controller.getPadController(padId);
		padController.setStatus(PadStatus.PLAYING);

		assertThat(controller.isAtLeastOnePadPlaying()).isTrue();
	}

	// Swap

	private static Stream<Arguments> parameters()
	{
		return Stream.of(
				of(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), UUID.fromString("57accabc-7d19-473c-a0a1-ea5c61b85e18")),
				of(UUID.fromString("27a04608-a5f0-4d63-9ac0-5f7187e9bd15"), UUID.fromString("fc427184-2e55-4734-8148-5fb657963616")),
				of(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), UUID.fromString("94ad54b2-995a-4da6-87c8-b86e5b97d03e"))
		);
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testSwapPads(UUID padId1, UUID padId2)
	{
		final int pad1Position = project.getPad(padId1).getPosition();
		final int pad2Position = project.getPad(padId2).getPosition();

		controller.swapPads(padId1, padId2);

		assertThat(project.getPad(padId2).getPosition()).isEqualTo(pad1Position);
		assertThat(project.getPad(padId1).getPosition()).isEqualTo(pad2Position);
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testReplacePad(UUID padId1, UUID padId2)
	{
		controller.replacePad(project.getPad(padId1), padId2);

		assertThat(project.getPad(padId2)).isNull();

	}
}
