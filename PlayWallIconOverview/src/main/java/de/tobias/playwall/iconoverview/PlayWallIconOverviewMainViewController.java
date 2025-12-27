package de.tobias.playwall.iconoverview;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.iconoverview.parser.EnumUsageParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.thecodelabs.utils.util.Localization.getString;

@Getter(AccessLevel.PACKAGE)
public class PlayWallIconOverviewMainViewController extends NVC
{
	@FXML
	private Label labelTitle;

	@FXML
	private TextField textFieldSearch;

	@FXML
	private ListView<IconEntry> listView;

	private final App app;

	public PlayWallIconOverviewMainViewController(Stage stage, App app)
	{
		this.app = app;

		load("de/tobias/playwall/iconoverview/view", "MainView.fxml", Localization.getBundle());
		final NVCStage nvcStage = applyViewControllerToStage(stage);
		nvcStage.setImage(PlayWallIconOverviewMain.icon);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		stage.setTitle(app.getInfo().getName());
		stage.setMinWidth(400);
		stage.setMinHeight(400);
		stage.setWidth(800);
		stage.setHeight(600);
		stage.setResizable(true);
		stage.centerOnScreen();

		labelTitle.setText(app.getInfo().getName());

		listView.setPlaceholder(new Label(getString("list.placeholder")));
		listView.setCellFactory(_ -> new IconCell());

		final FilteredList<IconEntry> filteredData = new FilteredList<>(getData(), s -> true);

		listView.setItems(filteredData);

		textFieldSearch.textProperty().addListener((_, oldValue, newValue) -> {
			if(newValue == null || newValue.isEmpty())
			{
				filteredData.setPredicate(s -> true);
			}
			else
			{
				final String newValueLowerCase = newValue.toLowerCase();
				filteredData.setPredicate(s -> s.description().toLowerCase().contains(newValueLowerCase) || s.fontIconType().toString().toLowerCase().contains(newValueLowerCase));
			}
		});
	}

	private ObservableList<IconEntry> getData()
	{
		final ObservableList<IconEntry> data = FXCollections.observableArrayList();
		data.add(new IconEntry(FontAwesomeType.FLOPPY_DISK_SOLID, "Speichern Button", true));

		final Set<String> declaredIconTypes = data.stream()
				.map(IconEntry::fontIconType)
				.map(Object::toString)
				.collect(Collectors.toSet());
		final Set<String> usedIconTypes = getUsedIconTypes();
		usedIconTypes.removeAll(declaredIconTypes);

		for(String usedIconType : usedIconTypes)
		{
			try
			{
				final FontAwesomeType fontIconType = FontAwesomeType.valueOf(usedIconType);
				data.add(new IconEntry(fontIconType, null, false));
			}
			catch(IllegalArgumentException e)
			{
				Logger.error(e);
			}
		}

		FXCollections.sort(data, Comparator.comparing((e) -> e.fontIconType().toString()));

		return data;
	}

	private Set<String> getUsedIconTypes()
	{
		final Path sourceRoot = Paths.get(System.getProperty("user.dir") + "/PlayWallClient/src/main/java");
		try
		{
			final Set<String> usedValues = EnumUsageParser.parse(sourceRoot, FontAwesomeType.class.getSimpleName());
			for(String value : usedValues)
			{
				Logger.info(value);
			}
			return usedValues;
		}
		catch(IOException e)
		{
			Logger.error("Error parsing used icon types", e);
			return new HashSet<>();
		}
	}
}
