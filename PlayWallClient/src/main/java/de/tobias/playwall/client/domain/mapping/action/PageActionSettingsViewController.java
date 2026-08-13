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
	private ComboBox<PageAction.PageActionType> typeComboBox;

	@FXML
	private SettingsRow pageNumberRow;
	@FXML
	private NumberTextField pageNumber;

	@Override
	protected void init()
	{
		typeComboBox.getItems().addAll(PageAction.PageActionType.values());
		typeComboBox.setCellFactory(_ -> new EnumCell<>("PageActionType."));
		typeComboBox.setButtonCell(new EnumCell<>("PageActionType."));

		pageNumberRow.visibleProperty().bind(typeComboBox.getSelectionModel().selectedItemProperty()
				.isEqualTo(PageAction.PageActionType.JUMP));
	}

	@Override
	public Action createNewAction()
	{
		return new PageAction();
	}

	@Override
	public void initAction(Action action)
	{
		if(action instanceof PageAction pageAction)
		{
			typeComboBox.getSelectionModel().select(pageAction.getPageActionType());
			if(pageAction.getPageActionType() == PageAction.PageActionType.JUMP)
			{
				pageNumber.setText(String.valueOf(pageAction.getPageNumber()));
			}
		}
	}

	@Override
	public void saveAction(Action action)
	{
		if(action instanceof PageAction pageAction)
		{
			pageAction.setPageActionType(typeComboBox.getSelectionModel().getSelectedItem());
			if(pageAction.getPageActionType() == PageAction.PageActionType.JUMP)
			{
				pageAction.setPageNumber(Integer.parseInt(pageNumber.getText()));
			}
		}
	}
}
