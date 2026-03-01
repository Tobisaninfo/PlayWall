package de.tobias.playwall.client.appcontext.loader;

import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.appcontext.*;
import io.github.classgraph.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AppContextLoader
{
	public static void setupDependencies(AppContext appContext)
	{
		setupDependencies(new AppContextLoaderRequest().withAppContext(appContext).withBasePackages(PlayWallMain.class.getPackage().getName()));
	}

	@SuppressWarnings({"java:S3011", "unchecked", "rawtypes"})
	public static void setupDependencies(AppContextLoaderRequest request)
	{
		final long start = System.currentTimeMillis();
		final AppContext appContext = request.getAppContext();

		final List<Class<? extends Annotation>> annotations = List.of(Service.class, ViewController.class);

		try(ScanResult scanResult = new ClassGraph()
				.enableAnnotationInfo()
				.ignoreClassVisibility()
				.acceptPackages(request.getBasePackages())
				.rejectPackages(request.getRejectPackages())
				.scan())
		{
			for(ClassInfo classInfo : scanResult.getClassesWithAnyAnnotation(annotations.toArray(Class[]::new)))
			{
				final Class loadedClass = classInfo.loadClass();

				final AnnotationInfo annotationInfo = annotations.stream().filter(classInfo::hasAnnotation).map(classInfo::getAnnotationInfo).findFirst().orElseThrow();
				final AnnotationParameterValueList annotationValues = annotationInfo.getParameterValues();

				Class superclass = ((AnnotationClassRef) annotationValues.get("superclass").getValue()).loadClass();
				if(superclass.equals(Object.class))
				{
					superclass = loadedClass;
				}

				final Constructor injectConstructor = getInjectConstructor(loadedClass);
				injectConstructor.setAccessible(true);

				boolean isSingleton = (boolean) annotationValues.get("singleton").getValue();

				final Function<AppContext, ?> loadFunction = request.getComponentInitializer().create(loadedClass, injectConstructor);
				if(isSingleton)
				{
					appContext.registerLazySingleton(superclass, loadFunction);
					log.debug("Registering singleton component {}", superclass);
				}
				else
				{
					appContext.registerLazy(superclass, loadFunction);
					log.debug("Registering component {}", superclass);
				}
			}
		}
		log.info("Dependency injection setup took {}ms", System.currentTimeMillis() - start);
	}

	private static Constructor<?> getInjectConstructor(Class<?> loadedClass)
	{
		return Arrays.stream(loadedClass.getDeclaredConstructors())
				.filter(c -> c.isAnnotationPresent(InjectConstructor.class))
				.findFirst()
				.orElseGet(() -> {
					try
					{
						return loadedClass.getDeclaredConstructor();
					}
					catch(NoSuchMethodException e)
					{
						throw new ComponentInitializationException("No suitable constructor found for " + loadedClass, e);
					}
				});
	}
}
