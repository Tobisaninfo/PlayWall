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
	private final App app;

	private CommandLine cmd;

	public static final Option DEBUG = new Option(null, "debug", false, "Debug flag");
	public static final Option STANDALONE = new Option(null, "standalone", false, "Do not start embedded server");
	public static final Option SERVER_PATH = new Option(null, "server-path", true, "Specify path to embedded server");

	@PostConstruct
	void initialize() throws IOException
	{
		final Options options = new Options();
		options.addOption(DEBUG);
		options.addOption(STANDALONE);
		options.addOption(SERVER_PATH);

		try
		{
			CommandLineParser parser = new DefaultParser();
			cmd = parser.parse(options, app.getProgramArgs());
		}
		catch(ParseException _)
		{
			HelpFormatter formatter = HelpFormatter.builder().get();
			formatter.printHelp(app.getInfo().getName(), null, options, null, true);
			System.exit(1);
		}
	}

	public boolean hasOption(Option option)
	{
		return cmd.hasOption(option.getOpt());
	}

	public String getOptionValue(Option option)
	{
		return cmd.getOptionValue(option);
	}
}
