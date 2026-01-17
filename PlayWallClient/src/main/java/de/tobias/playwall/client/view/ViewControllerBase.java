package de.tobias.playwall.client.view;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.tobias.playwall.client.view.style.AppIconProvider;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.view.style.Styleable;
import javafx.stage.Stage;

public class ViewControllerBase extends NVC
{
	@InjectField
	protected Styleable styleable;

	@InjectField
	protected AppIconProvider iconProvider;

	@InjectField
	protected AppContext.Environment environment;

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		styleable.applyToStage(stage);
		stage.getIcons().add(iconProvider.getStageIcon());
	}
}
