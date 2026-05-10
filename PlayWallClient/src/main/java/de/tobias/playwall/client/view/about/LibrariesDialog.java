package de.tobias.playwall.client.view.about;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.ModalDialogBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ViewController(path = "de/tobias/playwall/client/view/about", view = "LibrariesDialog")
@Slf4j
public class LibrariesDialog extends ModalDialogBase<Void>
{
	@FXML
	private TableView<LibraryEntry> table;
	@FXML
	private TableColumn<LibraryEntry, String> colName;
	@FXML
	private TableColumn<LibraryEntry, String> colVersion;
	@FXML
	private TableColumn<LibraryEntry, String> colLicense;

	@Override
	public void init()
	{
		colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
		colVersion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().version()));
		colLicense.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().license()));

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		table.setItems(loadLibraries());
	}

	private ObservableList<LibraryEntry> loadLibraries()
	{
		final List<LibraryEntry> entries = new ArrayList<>();
		try(InputStream is = app.getClasspathResource("license/THIRD-PARTY.csv").getInputStream())
		{
			if(is == null)
			{
				log.warn("THIRD-PARTY.csv not found on classpath");
				return FXCollections.emptyObservableList();
			}
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
			{
				String line;
				while((line = reader.readLine()) != null)
				{
					final String trimmed = line.trim();
					if(trimmed.isEmpty())
					{
						continue;
					}
					final String[] parts = trimmed.split("\\|", 3);
					if(parts.length == 3)
					{
						entries.add(new LibraryEntry(parts[0], parts[1], parts[2]));
					}
				}
			}
		}
		catch(IOException e)
		{
			log.error("Cannot load THIRD-PARTY.csv", e);
		}
		return FXCollections.observableList(entries);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stage.setTitle(Localization.getString(Strings.UI_DIALOG_LIBRARIES_TITLE));
		stage.setWidth(640);
		stage.setMinWidth(640);
		stage.setHeight(460);
		stage.setMinHeight(460);
		stage.setResizable(true);
	}
}
