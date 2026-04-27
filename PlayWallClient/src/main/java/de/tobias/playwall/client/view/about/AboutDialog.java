package de.tobias.playwall.client.view.about;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationInfo;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.AppUserInfoStrings;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.ModalDialogBase;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static de.tobias.playwall.client.appcontext.AppContext.Environment.GUI_TESTING;

@Getter(AccessLevel.PACKAGE)
@ViewController(path = "de/tobias/playwall/client/view/main", view = "AboutDialog")
@Slf4j
public class AboutDialog extends ModalDialogBase<Void>
{
	@FXML
	private Label libsLabel;

	@FXML
	private HBox codeContainer;

	@FXML
	private Label versionLabel;

	@FXML
	private Label platformLabel;

	@FXML
	private Label authorLabel;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private HBox websiteContainer;

	@FXML
	private Label graphicsLabel;

	@Override
	public void init()
	{
		final ApplicationInfo info = app.getInfo();

		versionLabel.setText(info.getVersion());
		authorLabel.setText(info.getAuthor());
		graphicsLabel.setText(Localization.getString(Strings.UI_DIALOG_ABOUT_GRAPHICS));
		libsLabel.setText(Localization.getString(Strings.UI_DIALOG_ABOUT_LIBRARIES));

		platformLabel.setText(String.format("%s (%s) + %s (%s)", System.getProperty("java.version"),
				System.getProperty("java.vendor"), System.getProperty("javafx.version"),
				System.getProperty("javafx.runtime.version")));

		final Hyperlink websiteLink = new Hyperlink(Localization.getString(Strings.UI_DIALOG_ABOUT_WEBSITE));
		websiteLink.setPadding(Insets.EMPTY);
		websiteLink.setFocusTraversable(false);
		websiteLink.setOnAction(e -> {
			String url = app.getUserInfo(AppUserInfoStrings.class).website();
			openWebsite(url);
		});
		websiteContainer.getChildren().add(websiteLink);

		final Hyperlink codeLink = new Hyperlink(Localization.getString(Strings.UI_DIALOG_ABOUT_CODE));
		codeLink.setPadding(Insets.EMPTY);
		codeLink.setFocusTraversable(false);
		codeLink.setOnAction(e -> {
			String url = app.getUserInfo(AppUserInfoStrings.class).repository();
			openWebsite(url);
		});
		codeContainer.getChildren().add(codeLink);
	}

	private void openWebsite(String url)
	{
		if(Desktop.isDesktopSupported())
		{
			try
			{
				Desktop.getDesktop().browse(new URI(url));
			}
			catch(IOException | URISyntaxException e)
			{
				log.error("Cannot open link", e);
			}
		}
	}

	@Override
	@SuppressWarnings("java:S1874")
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		addCloseKeyShortcut(stageContainer::close);

		stage.setResizable(false);
		stageContainer.initStyle(environment == GUI_TESTING ? StageStyle.UNDECORATED : StageStyle.EXTENDED);

		stage.setWidth(650);
		stage.setHeight(400);

		stage.getScene().setFill(Color.TRANSPARENT);
	}

	@Override
	public Optional<Void> showAndWait(Window owner)
	{
		final Stage stage = getStageContainer().orElseThrow().getStage();

		final double centerXPosition = owner.getX() + owner.getWidth() / 2d;
		final double centerYPosition = owner.getY() + owner.getHeight() / 2d;

		stage.setX(centerXPosition - stage.getWidth() / 2d);
		stage.setY(centerYPosition - stage.getHeight() / 2d);

		return super.showAndWait(owner);
	}
}
