package de.tobias.playwall.client.view;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.view.style.AppIconProvider;
import de.tobias.playwall.client.view.style.Styleable;
import javafx.collections.ObservableMap;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

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

		ObservableMap<KeyCombination, Runnable> accelerators = stage.getScene().getAccelerators();
		Runnable openDevTools = () -> ScenicView.show(stage.getScene());
		accelerators.put(new KeyCodeCombination(KeyCode.F12, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), openDevTools);
	}
}
