package iconoverview;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.tobias.playwall.iconoverview.IconDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class MissingIconDeclarationTest
{
	@BeforeAll
	public static void beforeAll()
	{
		Logger.init(Paths.get("."));
		Logger.setLevelFilter(LogLevelFilter.DEBUG);
		Logger.setFileOutput(FileOutputOption.DISABLED);
	}

	@Test
	void testMissingIconDeclaration()
	{
		final IconDeclaration iconDeclaration = new IconDeclaration(Paths.get(System.getProperty("user.dir")).resolve("../PlayWallClient/src/main"));

		final Set<String> usedIconTypes = iconDeclaration.getUsedIconTypes();
		assertThat(usedIconTypes)
				.withFailMessage("No used icon types found. Is the source root path set correctly?")
				.isNotEmpty();

		final Set<String> nonDeclaredIconTypes = iconDeclaration.getNonDeclaredIconTypes();
		assertThat(nonDeclaredIconTypes)
				.withFailMessage("Found used icon types that are missing in the icon declaration in PlayWallIconOverview: " + nonDeclaredIconTypes)
				.isEmpty();
	}
}
