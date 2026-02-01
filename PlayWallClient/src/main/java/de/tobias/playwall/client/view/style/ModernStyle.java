package de.tobias.playwall.client.view.style;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadIndex;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.utils.Minifier;
import de.tobias.playwall.client.view.components.PseudoClasses;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service(superclass = Styleable.class)
public class ModernStyle implements Styleable
{
	@InjectField
	private App app;

	@InjectField
	private CommandLineOptions commandLineOptions;

	private String globalTemplateString;
	private String padTemplateString;

	private Set<Stage> stages = new LinkedHashSet<>();

	@SneakyThrows
	@PostConstruct
	void init()
	{
		globalTemplateString = Minifier.minifyCss(app.getClasspathResource("style/template-modern-global.css").getAsString());
		padTemplateString = Minifier.minifyCss(app.getClasspathResource("style/template-modern-pad.css").getAsString());

		if(commandLineOptions.hasOption(CommandLineOptions.WATCH_STYLESHEETS))
		{
			final Path modernStylesheet = Paths.get(getClass().getClassLoader().getResource("style").toURI());
			final WatchService watchService = FileSystems.getDefault().newWatchService();
			modernStylesheet.register(
					watchService,
					StandardWatchEventKinds.ENTRY_MODIFY
			);

			final Thread watcher = new Thread(() -> watchForChanges(watchService));
			watcher.setDaemon(true);
			watcher.start();
		}
	}

	private void watchForChanges(WatchService watchService)
	{
		try
		{
			while(!Thread.currentThread().isInterrupted())
			{
				final WatchKey key = watchService.take();
				key.pollEvents();
				Platform.runLater(() -> {
					this.stages.forEach(this::applyToStage);
					Logger.debug("Reloaded Stylesheets");
				});
				key.reset();
			}
		}
		catch(InterruptedException _)
		{
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void applyToStage(Stage stage)
	{
		stages.add(stage);

		stage.getScene().getStylesheets().remove("style/modern.css");

		stage.getScene().getStylesheets().addFirst("style/modern.css");
	}

	@Override
	public void renderStylesheets(Stage stage, Page page, ProjectMetadata projectMetadata)
	{
		final Path renderedCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "generated_project.css");

		final StringBuilder stringBuilder = new StringBuilder();
		renderGlobalTemplate(stringBuilder, projectMetadata);
		page.getPads().forEach(pad -> renderPadTemplate(stringBuilder, new PadIndex(pad.getPosition(), page.getPosition()), pad));

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

	private void renderGlobalTemplate(StringBuilder builder, ProjectMetadata projectMetadata)
	{
		builder.append(renderGlobalTemplate(projectMetadata.getDefaultColor(), ""))
				.append(renderGlobalTemplate(projectMetadata.getPlayColor(), MessageFormat.format(":{0}", PseudoClasses.PLAY_CLASS.getPseudoClassName())));
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

	private void renderPadTemplate(StringBuilder builder, PadIndex padIndex, Pad pad)
	{
		if(pad.getDefaultColor() != null)
		{
			builder.append(renderPadTemplate(padIndex, pad.getDefaultColor(), ""));
		}

		if(pad.getPlayColor() != null)
		{
			builder.append(renderPadTemplate(padIndex, pad.getPlayColor(), MessageFormat.format(":{0}", PseudoClasses.PLAY_CLASS.getPseudoClassName())));
		}
	}

	private String renderPadTemplate(PadIndex padIndex, ModernColor color, String pseudoClass)
	{
		final Map<String, String> values = new HashMap<>();
		values.put("prefix", String.valueOf(padIndex));
		values.put("class", pseudoClass);
		values.put("buttonColor", color.getButtonColor());
		values.put("playbarTrackColor", color.getPlaybarColor());
		values.put("playbarBarColor", color.getPlaybarTrackColor());

		values.put("padColor", color.paint());
		values.put("padCueInColor", ModernColor.BLUE1.paint()); // TODO: From configuruation

		values.put("fontColor", color.getFontColor());
		return CssTemplateProcessor.render(padTemplateString, values);
	}
}
