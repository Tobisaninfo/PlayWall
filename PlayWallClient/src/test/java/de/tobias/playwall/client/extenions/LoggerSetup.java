package de.tobias.playwall.client.extenions;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Paths;

public class LoggerSetup implements BeforeAllCallback
{
	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		Logger.init(Paths.get("."));
		Logger.setLevelFilter(LogLevelFilter.DEBUG);
		Logger.setFileOutput(FileOutputOption.DISABLED);
	}
}
