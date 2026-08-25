package de.tobias.playwall.client.view.settings;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.domain.project.view.settings.BaseProjectSettingsViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.ParamDialogBase;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.PseudoClasses;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base class for settings dialogs.
 */
public abstract class BaseSettingsDialogController<P> extends ParamDialogBase<P>
{
	@FXML
	protected PlayWallButton saveButton;

	@FXML
	protected VBox boxCategories;

	@FXML
	private VBox settingsPageContainer;

	@FXML
	protected HBox boxButtons;

	@Getter(AccessLevel.NONE)
	protected final FluentClient client;

	protected final List<BaseSettingsViewController<P>> settingViewController = new ArrayList<>();

	protected Stage stage;

	protected final ErrorAlertBuilder errorAlertBuilder;

	@InjectConstructor
	protected BaseSettingsDialogController(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		this.client = client;
		this.errorAlertBuilder = errorAlertBuilder;
	}

	protected SettingsCategory createSettingsCategory(Class<? extends BaseSettingsViewController<P>> controllerClass, String localizationKey, FontAwesomeType icon)
	{
		final BaseSettingsViewController<P> viewController = AppContextHolder.getInstance().get(controllerClass);
		settingViewController.add(viewController);

		final SettingsCategory category = new SettingsCategory(Localization.getString(localizationKey), icon, viewController);
		category.setOnAction(this::onSelectCategory);
		boxCategories.getChildren().add(category);
		return category;
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		this.stage = stage;

		stage.setResizable(true);

		stage.setWidth(850);
		stage.setHeight(500);

		stage.setMinWidth(850);
		stage.setMinHeight(500);
	}

	private void onSelectCategory(ActionEvent event)
	{
		selectCategory((SettingsCategory) event.getSource());
	}

	@SuppressWarnings("unchecked")
	protected BaseSettingsViewController<P> selectCategory(SettingsCategory category)
	{
		boxCategories.getChildren().forEach(c -> c.pseudoClassStateChanged(PseudoClasses.SELECTED, false));
		boxCategories.getChildren().stream()
				.filter(c -> c.equals(category))
				.findFirst()
				.ifPresent(c -> c.pseudoClassStateChanged(PseudoClasses.SELECTED, true));

		settingsPageContainer.getChildren().setAll(category.getSettingsPageController().getSettingsPage());
		return (BaseSettingsViewController<P>) category.getSettingsPageController();
	}

	public BaseSettingsViewController<P> selectCategory(Class<? extends BaseProjectSettingsViewController> settingsViewController)
	{
		final Optional<Node> categoryOptional = boxCategories.getChildren().stream()
				.filter(SettingsCategory.class::isInstance)
				.filter(c -> ((SettingsCategory) c).getSettingsPageController().getClass().equals(settingsViewController))
				.findAny();
		return categoryOptional.map(node -> selectCategory((SettingsCategory) node)).orElse(null);
	}

	public BaseSettingsViewController<P> selectCategory(int index)
	{
		return selectCategory((SettingsCategory) boxCategories.getChildren().get(index));
	}

	protected void initButtons()
	{
		final BooleanBinding allValidBinding = Bindings.createBooleanBinding(
				() -> settingViewController.stream()
						.allMatch(vc -> vc.getIsValidProperty().get()),
				settingViewController.stream()
						.map(BaseSettingsViewController::getIsValidProperty)
						.toArray(Observable[]::new)
		);

		saveButton.disableProperty().bind(allValidBinding.not());
	}

	@FXML
	protected abstract void saveButtonHandler(ActionEvent event);

	@FXML
	protected void cancelButtonHandler(ActionEvent event)
	{
		settingViewController.forEach(BaseSettingsViewController::cleanup);

		getStageContainer().ifPresent(NVCStage::close);
	}
}
