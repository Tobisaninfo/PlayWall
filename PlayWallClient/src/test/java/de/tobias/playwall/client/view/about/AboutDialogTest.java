package de.tobias.playwall.client.view.about;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.AbstractViewControllerTest;
import javafx.application.Platform;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.assertions.api.Assertions.assertThat;

class AboutDialogTest extends AbstractViewControllerTest
{
	private AppContext context;

	private AboutDialog aboutDialog;
	private Stage stage;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);
	}

	@Test
	void testAboutDialog()
	{
		Platform.runLater(() -> {
			aboutDialog = context.get(AboutDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(aboutDialog.getVersionLabel()).hasText("0.0.1");
		assertThat(aboutDialog.getAuthorLabel()).hasText("TheCodeLabs");
		assertThat(aboutDialog.getGraphicsLabel()).hasText("Robert Goldmann");
		assertThat(aboutDialog.getLibsLabel()).hasText("ControlsFX (8.40.10), dom4j (1.6.1), snakeyaml (1.11), guava (15.0), gagawa (1.0.1), TinySound (1.1.1), JLayer (1.0.1), JSPF (1.0.2), json-smart (1.2).");
		assertThat(aboutDialog.getPlatformLabel().getText()).isNotBlank();
		assertThat(((Hyperlink) aboutDialog.getWebsiteContainer().getChildren().get(1)).getText()).isEqualTo("https://playwall.thecodelabs.de");
		assertThat(((Hyperlink) aboutDialog.getCodeContainer().getChildren().get(1)).getText()).isEqualTo("https://thecodelabs.de/PlayWall/PlayWall");
	}
}
