package de.tobias.playwall.client.viewcontroller.style;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.utils.Minifier;
import de.tobias.playwall.client.view.components.PseudoClasses;
import de.tobias.playwall.client.viewcontroller.style.color.ModernColor;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Service(superclass = Styleable.class)
public class ModernStyle implements Styleable
{
	@InjectField
	private App app;

	private String globalTemplateString;
	private String padTemplateString;

	@PostConstruct
	void init()
	{
		globalTemplateString = Minifier.minify(app.getClasspathResource("style/template-modern-global.css").getAsString());
		padTemplateString = Minifier.minify(app.getClasspathResource("style/template-modern-pad.css").getAsString());
	}

	@Override
	public void applyToStage(Stage stage)
	{
		stage.getScene().getStylesheets().add("style/style.css");
		stage.getScene().getStylesheets().add("style/modern.css");
		stage.getScene().getStylesheets().add("style/settings.css");
	}

	@Override
	public void renderStylesheets(Stage stage, Page page)
	{
		final Path renderedCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "generated_project.css");

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(renderGlobalTemplate());
//		page.getPads().forEach(pad -> stringBuilder.append(renderPadTemplate(pad)));

		try
		{
			Files.write(renderedCss, stringBuilder.toString().getBytes());
		}
		catch(IOException e)
		{
			Logger.error(e);
		}

		stage.getScene().getStylesheets().remove(renderedCss.toUri().toString());
		stage.getScene().getStylesheets().add(renderedCss.toUri().toString());
	}

	private String renderGlobalTemplate()
	{
		return String.join(
				renderGlobalTemplate(ModernColor.GRAY1, ""),
				renderGlobalTemplate(ModernColor.RED1, MessageFormat.format(":{0}", PseudoClasses.PLAY_CLASS.getPseudoClassName())),
				renderGlobalTemplate(ModernColor.ORANGE1, MessageFormat.format(":{0}", PseudoClasses.WARN_CLASS.getPseudoClassName()))
		);
	}

	private String renderGlobalTemplate(ModernColor color, String pseudoClass)
	{
		final Map<String, String> values = new HashMap<>();
		values.put("class", pseudoClass);
		values.put("buttonColor", color.getButtonColor());
		values.put("playbarTrackColor", color.getPlaybarColor());
		values.put("playbarBarColor", color.getPlaybarTrackColor());

		values.put("padColor", color.paint());
		values.put("padCueInColor", ModernColor.BLUE1.paint()); // TODO: From configuruation

		values.put("fontColor", color.getFontColor());
		values.put("infoFontSize", String.valueOf(13)); // TODO: From configuruation
		values.put("titleFontSize", String.valueOf(13)); // TODO: From configuruation

		return CssTemplateProcessor.render(globalTemplateString, values);
	}

	private String renderPadTemplate(Pad pad)
	{
		return CssTemplateProcessor.render(padTemplateString, Map.of());
	}
}
