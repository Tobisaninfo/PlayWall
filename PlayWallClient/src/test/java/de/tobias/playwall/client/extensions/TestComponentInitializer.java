package de.tobias.playwall.client.extensions;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.appcontext.loader.ComponentInitializer;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;

public class TestComponentInitializer<T> extends ComponentInitializer<T>
{
	protected TestComponentInitializer(Class<T> loadedClass, Constructor<T> injectConstructor)
	{
		super(loadedClass, injectConstructor);
	}

	@Override
	protected void applyViewToStage(AppContext context, ViewController annotation, NVC nvc)
	{
		final Stage stage = context.get(Stage.class);
		nvc.applyViewControllerToStage(stage);
	}
}
