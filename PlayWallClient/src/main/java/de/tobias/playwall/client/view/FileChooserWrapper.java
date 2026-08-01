package de.tobias.playwall.client.view;

import com.google.gson.JsonElement;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.common.utils.FileFormats;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
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

	public FileChooserWrapper setInitialFilename(String filename)
	{
		fileChooser.setInitialFileName(filename);
		return this;
	}

	public Optional<Path> showOpenFile(Window owner)
	{
		selectLastUsedFolder();

		final File selectedFile = fileChooser.showOpenDialog(owner);
		if(selectedFile != null)
		{
			app.getUserDefaults().setData(OPEN_FOLDER, selectedFile.getParent());
		}
		return Optional.ofNullable(selectedFile).map(File::toPath);
	}

	public Optional<Path> showSaveFile(Window owner)
	{
		selectLastUsedFolder();
		final File selectedFile = fileChooser.showSaveDialog(owner);
		if(selectedFile != null)
		{
			app.getUserDefaults().setData(OPEN_FOLDER, selectedFile.getParent());
		}
		return Optional.ofNullable(selectedFile).map(File::toPath);
	}

	private void selectLastUsedFolder()
	{
		getLastUsedFolder().ifPresent(fileChooser::setInitialDirectory);
	}

	private Optional<File> getLastUsedFolder()
	{
		final JsonElement openFolder = app.getUserDefaults().getData(OPEN_FOLDER);
		if(openFolder != null)
		{
			final File folder = new File(openFolder.getAsString());
			if(folder.exists())
			{
				return Optional.of(folder);
			}
		}
		return Optional.empty();
	}

	public Optional<Path> showOpenFolder(Window owner)
	{
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		getLastUsedFolder().ifPresent(directoryChooser::setInitialDirectory);

		final File selectedFolder = directoryChooser.showDialog(owner);
		if(selectedFolder != null)
		{
			app.getUserDefaults().setData(OPEN_FOLDER, selectedFolder.getPath());
		}
		return Optional.ofNullable(selectedFolder).map(File::toPath);
	}

	public Optional<Path> showByActionEvent(ActionEvent event)
	{
		setExtensionFilter(FileFormats.FILE_FORMATS.stream().map(format ->
				new FileChooser.ExtensionFilter(
						Localization.getString("FileFormat." + format.contentType().name()),
						format.extensions().stream().map(ext -> "*." + ext).toList()
				)).toList());

		final Window owner = ((Node) event.getTarget()).getScene().getWindow();
		return showOpenFile(owner);
	}
}
