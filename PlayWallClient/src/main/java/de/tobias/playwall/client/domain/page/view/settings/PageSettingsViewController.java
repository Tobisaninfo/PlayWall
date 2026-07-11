package de.tobias.playwall.client.domain.page.view.settings;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.ColorMapper;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsDialogController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Viewcontroller for the page settings dialog.
 * Holds the sidebar consisting of {@link SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/page", view = "PageSettingsView")
@Slf4j
public class PageSettingsViewController extends BaseSettingsDialogController<BasePageSettingsViewController.Param>
{
	@Getter(AccessLevel.NONE)
	private Page page;

	private final ColorMapper colorMapper;

	@InjectConstructor
	public PageSettingsViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder, ColorMapper colorMapper)
	{
		super(client, errorAlertBuilder);
		this.colorMapper = colorMapper;
	}

	@Override
	protected void init()
	{
		boxCategories.getStyleClass().add("settings-category-box");

		final SettingsCategory categoryGeneral = createSettingsCategory(PageSettingsGeneralViewController.class, Strings.UI_SETTINGS_PAGE_GENERAL_TITLE, FontAwesomeType.GEAR_SOLID);

		initButtons();

		selectCategory(categoryGeneral);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		stage.setWidth(600);
	}

	@Override
	public void initParameter(BasePageSettingsViewController.Param parameter)
	{
		this.page = parameter.page;

		settingViewController.forEach(controller -> controller.initParameter(new BasePageSettingsViewController.Param(page)));
		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAGE_TITLE, page.getName()));
	}

	@FXML
	protected void saveButtonHandler(ActionEvent event)
	{
		settingViewController.forEach(controller -> controller.applySettings(new BasePageSettingsViewController.Param(page)));

		try
		{
			client.currentProject().page(page.getId()).updateSettings(page.getName(), colorMapper.modernColorToColor(page.getColor()));
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot update page settings", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAGE_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}
}
