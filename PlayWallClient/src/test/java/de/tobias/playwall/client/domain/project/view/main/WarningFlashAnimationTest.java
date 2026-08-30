package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WarningFlashAnimationTest extends AbstractViewControllerTest
{
	private static final Color FROM_COLOR = ModernColor.BLUE1.getColor();
	private static final Color TO_COLOR = ModernColor.RED1.getColor();

	private DesktopPadView padView;
	private WarningFlashAnimation animation;

	@Start
	private void start(Stage stage)
	{
		padView = new DesktopPadView();
		final Pad pad = mock(Pad.class);
		when(pad.getId()).thenReturn(UUID.randomUUID());
		when(pad.getDefaultColor()).thenReturn(ModernColor.BLUE1);
		when(pad.getPlayColor()).thenReturn(ModernColor.RED1);

		padView.updateFromPad(0, new ClientPadController(new UpdateMessageEventHandler(), pad, mock(ProjectMetadata.class)));

		animation = new WarningFlashAnimation();
		animation.init(padView);

		stage.setScene(new Scene(new Pane(padView.getSuperRoot()), 200, 100));
		stage.show();
	}

	@Test
	void testAnimationStartStop()
	{
		// Start Animation
		animation.start();
		assertTrue(animation.isRunning());

		// Right after start the flash color equals the pad's default color
		WaitForAsyncUtils.sleep(50, MILLISECONDS);
		assertThat(animation.getCurrentColor()).isEqualTo(FROM_COLOR);

		// Half-way through the eased ramp, the color must lie strictly between the two
		// endpoints - proving a real color interpolation happens instead of a hard cut
		WaitForAsyncUtils.sleep(200, MILLISECONDS);
		final double midRed = animation.getCurrentColor().getRed();
		assertThat(midRed).isStrictlyBetween(
				Math.min(FROM_COLOR.getRed(), TO_COLOR.getRed()),
				Math.max(FROM_COLOR.getRed(), TO_COLOR.getRed()));

		// At the keyframe target, the color reaches the play color
		WaitForAsyncUtils.sleep(250, MILLISECONDS);
		assertThat(animation.getCurrentColor().getRed()).isCloseTo(TO_COLOR.getRed(), within(0.05));
		assertThat(animation.getCurrentColor().getGreen()).isCloseTo(TO_COLOR.getGreen(), within(0.05));
		assertThat(animation.getCurrentColor().getBlue()).isCloseTo(TO_COLOR.getBlue(), within(0.05));

		// And back to the default color after a full cycle
		WaitForAsyncUtils.sleep(500, MILLISECONDS);
		assertThat(animation.getCurrentColor().getRed()).isCloseTo(FROM_COLOR.getRed(), within(0.05));

		animation.stop();
		assertThat(animation.isRunning()).isFalse();
		assertThat(padView.getRootNode().getStyle()).isEmpty();
	}
}
