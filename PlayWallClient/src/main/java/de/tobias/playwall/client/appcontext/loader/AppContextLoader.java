package de.tobias.playwall.client.appcontext.loader;

import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.appcontext.*;
import io.github.classgraph.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
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

	public static void setupDependencies(AppContextLoaderRequest request)
	{
		final long start = System.currentTimeMillis();
		final AppContext appContext = request.getAppContext();

		try(ScanResult scanResult = new ClassGraph()
				.enableAnnotationInfo()
				.ignoreClassVisibility()
				.acceptPackages(request.getBasePackages())
				.rejectPackages(request.getRejectPackages())
				.scan())
		{
			loadBeans(request, scanResult, appContext);
			loadServicesAndViewControllers(request, scanResult, appContext);
		}
		log.info("Dependency injection setup took {}ms", System.currentTimeMillis() - start);
	}

	@SuppressWarnings({"java:S3011", "unchecked", "rawtypes"})
	private static void loadBeans(AppContextLoaderRequest request, ScanResult scanResult, AppContext appContext)
	{
		final List<Class<? extends Annotation>> annotations = List.of(Configuration.class);

		for(ClassInfo classInfo : scanResult.getClassesWithAnyAnnotation(annotations.toArray(Class[]::new)))
		{
			final Class configurationClass = classInfo.loadClass();

			final Constructor injectConstructor = getInjectConstructor(configurationClass);
			injectConstructor.setAccessible(true);

			final Function<AppContext, ?> configurationInitializer = request.getComponentInitializer().create(configurationClass, injectConstructor);
			appContext.registerLazySingleton(configurationClass, configurationInitializer);

			for(Method method : configurationClass.getDeclaredMethods())
			{
				if(!method.isAnnotationPresent(Bean.class))
				{
					continue;
				}

				final Class beanType = method.getReturnType();
				method.setAccessible(true);

				appContext.registerLazySingleton(beanType, context -> {
					final Object configurationInstance = context.get(configurationClass);
					try
					{
						return method.invoke(configurationInstance);
					}
					catch(IllegalAccessException | InvocationTargetException e)
					{
						throw new ComponentInitializationException(MessageFormat.format("Cannot invoke bean method {0} on {1}", method, configurationClass), e);
					}
				});
				log.debug("Registering bean {} from configuration {}", beanType, configurationClass);
			}
		}
	}

	@SuppressWarnings({"java:S3011", "unchecked", "rawtypes"})
	private static void loadServicesAndViewControllers(AppContextLoaderRequest request, ScanResult scanResult, AppContext appContext)
	{
		final List<Class<? extends Annotation>> annotations = List.of(Service.class, ViewController.class);

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
