package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.ui.scene.input.NumberTextField;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "PageActionSettingsView", applyToStage = false)
class PageActionSettingsViewController extends ActionSettingsViewController
{
	@FXML
	private ComboBox<PageAction.PageActionMode> pageModeComboBox;

	@FXML
	private SettingsRow pageNumberRow;
	@FXML
	private NumberTextField pageNumberTextField;

	@Override
	protected void init()
	{
		pageModeComboBox.getItems().addAll(PageAction.PageActionMode.values());
		pageModeComboBox.setCellFactory(_ -> new EnumCell<>(PageAction.PageActionMode.class));
		pageModeComboBox.setButtonCell(new EnumCell<>(PageAction.PageActionMode.class));

		pageNumberRow.visibleProperty().bind(pageModeComboBox.getSelectionModel().selectedItemProperty()
				.isEqualTo(PageAction.PageActionMode.JUMP));
	}

	@Override
	public Action createNewAction()
	{
		return new PageAction(PageAction.PageActionMode.PREVIOUS, null);
	}

	@Override
	public void initSettings(Action action)
	{
		if(action instanceof PageAction pageAction)
		{
			pageModeComboBox.getSelectionModel().select(pageAction.getPageActionMode());
			if(pageAction.getPageActionMode() == PageAction.PageActionMode.JUMP)
			{
				pageNumberTextField.setText(String.valueOf(pageAction.getPageNumber() + 1));
			}
		}
	}

	@Override
	public void applySettings(Action action)
	{
		if(action instanceof PageAction pageAction)
		{
			pageAction.setPageActionMode(pageModeComboBox.getSelectionModel().getSelectedItem());
			if(pageAction.getPageActionMode() == PageAction.PageActionMode.JUMP)
			{
				pageAction.setPageNumber(Integer.parseInt(pageNumberTextField.getText()) - 1);
			}
		}
	}
}
