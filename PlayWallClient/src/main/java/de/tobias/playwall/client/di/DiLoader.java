package de.tobias.playwall.client.di;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.PlayWallMain;
import io.github.classgraph.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiLoader
{
	@SuppressWarnings({"java:S112", "java:S3011", "java:S3740", "unchecked", "rawtypes"})
	public static void setupDependencies()
	{
		final long start = System.currentTimeMillis();

		final String basePackage = PlayWallMain.class.getPackage().getName();
		final String componentAnnotation = Component.class.getName();

		try(ScanResult scanResult = new ClassGraph()
				.enableAnnotationInfo()
				.enableClassInfo()
				.acceptPackages(basePackage)
				.scan())
		{
			for(ClassInfo componentClassInfo : scanResult.getClassesWithAnnotation(componentAnnotation))
			{
				final Class<?> loadedClass = componentClassInfo.loadClass();

				final AnnotationInfo annotationInfo = componentClassInfo.getAnnotationInfo(componentAnnotation);
				final AnnotationParameterValueList annotationValues = annotationInfo.getParameterValues();

				Class superclass = ((AnnotationClassRef) annotationValues.get("superclass").getValue()).loadClass();
				if(superclass.equals(Object.class))
				{
					superclass = loadedClass;
				}

				final Constructor<?> injectConstructor = Arrays.stream(loadedClass.getDeclaredConstructors())
						.filter(c -> c.isAnnotationPresent(InjectConstructor.class))
						.findFirst()
						.orElseGet(() -> {
							try
							{
								return loadedClass.getDeclaredConstructor();
							}
							catch(NoSuchMethodException e)
							{
								throw new RuntimeException("No suitable constructor found for " + loadedClass, e);
							}
						});

				injectConstructor.setAccessible(true);

				final Function<DI, ?> loadFunction = di -> {
					try
					{
						final Class<?>[] paramTypes = injectConstructor.getParameterTypes();
						final Object[] params = new Object[paramTypes.length];

						for(int i = 0; i < paramTypes.length; i++)
						{
							params[i] = di.get(paramTypes[i]);
							if(params[i] == null)
							{
								throw new RuntimeException("Missing dependency: " + paramTypes[i].getName() +
														   " for " + loadedClass.getName());
							}
						}

						return injectConstructor.newInstance(params);
					}
					catch(Exception e)
					{
						Logger.error(MessageFormat.format("Cannot instantiate component {0}", loadedClass));
						Logger.error(e);
						throw new RuntimeException(e);
					}
				};

				boolean isSingleton = (boolean) annotationValues.get("singleton").getValue();
				Logger.debug("Registering component {0}", superclass);

				if(isSingleton)
				{
					DI.instance().registerLazySingleton(superclass, loadFunction);
				}
				else
				{
					DI.instance().registerLazy(superclass, loadFunction);
				}
			}
		}
		Logger.info("Dependency injection setup took {0}ms", System.currentTimeMillis() - start);
	}
}
