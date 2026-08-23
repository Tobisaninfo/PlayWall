package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "PadActionSettingsView", applyToStage = false)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
class PadActionSettingsViewController extends ActionSettingsViewController
{
	private static final Page ALL_PAGES = new Page(null, new PageSettings(Localization.getString("ui.action.pad.settings.page.all"), ModernColor.GRAY1), null, null);

	@FXML
	private ComboBox<PadAction.PadActionMode> modeComboBox;

	@FXML
	private ComboBox<Page> pageComboBox;

	private final ClientProjectController projectController;

	@Override
	protected void init()
	{
		modeComboBox.getItems().addAll(PadAction.PadActionMode.values());
		final String modePrefix = PadAction.PadActionMode.class.getSimpleName() + ".";
		modeComboBox.setCellFactory(_ -> new EnumCell<>(modePrefix));
		modeComboBox.setButtonCell(new EnumCell<>(modePrefix));

		pageComboBox.setButtonCell(new PageCell());
		pageComboBox.setCellFactory(_ -> new PageCell());
	}

	@Override
	public Action createNewAction()
	{
		return new PadAction(PadAction.PadActionMode.PLAY_STOP, null);
	}

	@Override
	public void initSettings(Action action)
	{
		if(action instanceof PadAction padAction)
		{
			modeComboBox.getSelectionModel().select(padAction.getPadActionMode());

			final List<Page> pages = new ArrayList<>(projectController.getProject().getPages());
			pages.addFirst(ALL_PAGES);
			pageComboBox.getItems().setAll(pages);

			if(padAction.getPageId() == null)
			{
				pageComboBox.getSelectionModel().select(ALL_PAGES);
			}
			else
			{
				final Optional<Page> selectedPage = pages.stream()
						.filter(p -> p.getId() != null && p.getId().equals(padAction.getPageId()))
						.findFirst();
				if(selectedPage.isPresent())
				{
					pageComboBox.getSelectionModel().select(selectedPage.get());
				}
				else
				{
					pageComboBox.getSelectionModel().select(ALL_PAGES);
				}
			}
		}
	}

	@Override
	public void applySettings(Action action)
	{
		if(action instanceof PadAction padAction)
		{
			padAction.setPadActionMode(modeComboBox.getSelectionModel().getSelectedItem());
			padAction.setPageId(pageComboBox.getSelectionModel().getSelectedItem().getId());
		}
	}
}
