package de.tobias.playwall.client.di;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.PlayWallMain;
import io.github.classgraph.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiLoader
{
	@SuppressWarnings({"java:S112", "java:S3740", "unchecked", "rawtypes"})
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

				final Function<DI, ?> loadFunction = di -> {
					try
					{
						return loadedClass.getConstructor().newInstance();
					}
					catch(NoSuchMethodException | InstantiationException | IllegalAccessException |
						  InvocationTargetException e)
					{
						Logger.error(MessageFormat.format("Cannot register component {0}", loadedClass), e);
						throw new RuntimeException(e);
					}
				};

				Logger.debug("Registering component {0}", superclass);
				boolean isSingleton = (boolean) annotationValues.get("singleton").getValue();
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
