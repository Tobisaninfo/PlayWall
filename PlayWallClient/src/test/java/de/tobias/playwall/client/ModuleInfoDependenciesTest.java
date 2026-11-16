package de.tobias.playwall.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that every module required in the client's {@code module-info.java} (including project modules such as
 * {@code de.tobias.playwall.common}) can be found and is a real (explicit) module, not an automatic one.
 */
class ModuleInfoDependenciesTest
{
	private static final Path CLASSES_DIR = Path.of("target", "classes");

	private static ModuleDescriptor clientDescriptor;
	private static ModuleFinder finder;

	@BeforeAll
	static void setUp() throws IOException
	{
		try(InputStream in = Files.newInputStream(CLASSES_DIR.resolve("module-info.class")))
		{
			clientDescriptor = ModuleDescriptor.read(in);
		}

		final List<Path> searchPath = new ArrayList<>();
		searchPath.add(CLASSES_DIR);
		splitPath(System.getProperty("jdk.module.path")).forEach(searchPath::add);
		splitPath(System.getProperty("java.class.path")).forEach(searchPath::add);

		finder = ModuleFinder.compose(
				ModuleFinder.ofSystem(),
				ModuleFinder.of(searchPath.stream().filter(Files::exists).toArray(Path[]::new))
		);
	}

	private static Stream<Path> splitPath(String path)
	{
		if(path == null || path.isBlank())
			return Stream.empty();
		return Stream.of(path.split(java.io.File.pathSeparator)).map(Path::of);
	}

	@Test
	void testAllRequiredModulesAreRealModules()
	{
		final List<String> problems = new ArrayList<>();

		for(ModuleDescriptor.Requires requires : clientDescriptor.requires())
		{
			final String name = requires.name();
			final Optional<ModuleDescriptor> descriptor = finder.find(name).map(ModuleReference::descriptor);

			if(descriptor.isEmpty())
			{
				problems.add("%s: module not found".formatted(name));
			}
			else if(descriptor.get().isAutomatic())
			{
				problems.add("%s: is only an automatic module".formatted(name));
			}
		}

		assertThat(problems).as("Invalid requires in module-info.java").isEmpty();
	}

	@Test
	void testModuleGraphResolves()
	{
		final Configuration configuration = Configuration.empty().resolve(
				finder, ModuleFinder.of(), Set.of(clientDescriptor.name())
		);

		assertThat(configuration.findModule(clientDescriptor.name())).isPresent();
		assertThat(configuration.findModule("de.tobias.playwall.common")).isPresent();
	}
}
