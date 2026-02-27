package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WarningFlashAnimationTest extends AbstractViewControllerTest
{
	private DesktopPadView padView;
	private WarningFlashAnimation animation;
	private final PseudoClass warning = PseudoClass.getPseudoClass("warning");

	@Start
	private void start(Stage stage)
	{
		padView = new DesktopPadView();
		final Pad pad = mock(Pad.class);
		when(pad.getId()).thenReturn(UUID.randomUUID());

		padView.updateFromPad(0, new ClientPadController(pad, null));

		animation = new WarningFlashAnimation();
		animation.init(padView, warning);

		stage.setScene(new Scene(new Pane(padView.getSuperRoot()), 200, 100));
		stage.show();
	}

	@Test
	void testAnimationStartStop()
	{
		// Start Animation
		animation.start();
		assertTrue(animation.isRunning());

		WaitForAsyncUtils.sleep(600, MILLISECONDS);
		assertThat(padView.getPseudoClassStates()).contains(warning);

		WaitForAsyncUtils.sleep(500, MILLISECONDS);
		assertThat(padView.getPseudoClassStates()).doesNotContain(warning);

		WaitForAsyncUtils.sleep(600, MILLISECONDS);
		assertThat(padView.getPseudoClassStates()).contains(warning);

		WaitForAsyncUtils.sleep(500, MILLISECONDS);
		assertThat(padView.getPseudoClassStates()).doesNotContain(warning);

		animation.stop();
		assertThat(animation.isRunning()).isFalse();
		assertThat(padView.getPseudoClassStates()).doesNotContain(warning);
	}
}