package de.tobias.playwall.client;

import de.thecodelabs.utils.application.App;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.IOException;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = {@InjectConstructor})
public class CommandLineOptions
{
	private static final int DEFAULT_PORT = 10023;

	private final App app;

	private CommandLine cmd;

	public static final Option DEBUG = new Option(null, "debug", false, "Debug flag");
	public static final Option STANDALONE = new Option(null, "standalone", false, "Do not start embedded server");
	public static final Option SERVER_PORT = new Option(null, "port", true, "Server port. Only necessary if standalone is NOT set. Default: " + DEFAULT_PORT);
	public static final Option WATCH_STYLESHEETS = new Option(null, "watch-stylesheets", false, "Install a file watch for stylesheets");
	public static final Option SERVER_PATH = new Option(null, "server-path", true, "Specify path to embedded server");
	public static final Option PROJECT = new Option(null, "project", true, "Skip launch dialog and open the specified project immediately if existing");

	@PostConstruct
	void initialize() throws IOException
	{
		final Options options = new Options();
		options.addOption(DEBUG);
		options.addOption(STANDALONE);
		options.addOption(SERVER_PORT);
		options.addOption(WATCH_STYLESHEETS);
		options.addOption(SERVER_PATH);
		options.addOption(PROJECT);

		try
		{
			final CommandLineParser parser = new DefaultParser();
			cmd = parser.parse(options, app.getProgramArgs());
		}
		catch(ParseException _)
		{
			final HelpFormatter formatter = HelpFormatter.builder().get();
			formatter.printHelp(app.getInfo().getName(), null, options, null, true);
			System.exit(1);
		}
	}

	public boolean hasOption(Option option)
	{
		return cmd.hasOption(option);
	}

	public String getOptionValue(Option option)
	{
		return cmd.getOptionValue(option);
	}

	public int getServerPort()
	{
		if(cmd.hasOption(SERVER_PORT))
		{
			return Integer.parseInt(cmd.getOptionValue(SERVER_PORT));
		}

		return DEFAULT_PORT;
	}
}
