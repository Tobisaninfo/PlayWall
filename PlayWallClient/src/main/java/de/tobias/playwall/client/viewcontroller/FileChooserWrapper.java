package de.tobias.playwall.client.viewcontroller;

import com.google.gson.JsonElement;
import de.thecodelabs.utils.application.App;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.ViewController;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@ViewController(autoload = false, path = "", view = "")
public class FileChooserWrapper
{
	private static final String OPEN_FOLDER = "openFolder";

	private FileChooser fileChooser;

	private final App app;

	@InjectConstructor
	FileChooserWrapper(App app)
	{
		this.app = app;
	}

	@PostConstruct
	void init()
	{
		fileChooser = new FileChooser();
	}

	public FileChooserWrapper setExtensionFilter(List<FileChooser.ExtensionFilter> extensionFilters)
	{
		fileChooser.getExtensionFilters().setAll(extensionFilters);
		return this;
	}

	public Optional<Path> showOpenFile(Window owner)
	{
		// Last Folder
		final JsonElement openFolder = app.getUserDefaults().getData(OPEN_FOLDER);
		if(openFolder != null)
		{
			final File folder = new File(openFolder.getAsString());
			if(folder.exists())
			{
				fileChooser.setInitialDirectory(folder);
			}
		}

		final File selectedFile = fileChooser.showOpenDialog(owner);
		if(selectedFile != null)
		{
			app.getUserDefaults().setData(OPEN_FOLDER, selectedFile.getParent());
		}
		return Optional.ofNullable(selectedFile).map(File::toPath);
	}
}
