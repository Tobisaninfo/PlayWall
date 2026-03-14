package de.tobias.playwall.client.log;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.LogEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

@Service
public class LogStore
{
	private static final int MAX_ENTRIES = 10_000;

	@Getter
	private final ObservableList<LogEntry> allEntries = FXCollections.observableArrayList();
	@Getter
	private final ObservableList<String> sources = FXCollections.observableArrayList("Alle Quellen");

	public void onEntry(LogEntry entry)
	{
		Platform.runLater(() -> {
			if(allEntries.size() >= MAX_ENTRIES)
			{
				allEntries.remove(0, 500);
			}
			allEntries.add(entry);

			// Track sources
			if(!sources.contains(entry.getSource()))
			{
				sources.add(entry.getSource());
			}
		});
	}

	public void clearMessages()
	{
		allEntries.clear();
	}
}
