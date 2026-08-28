package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.components.PseudoClasses;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
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
	private static final Page ACTIVE_PAGE = new Page(PadAction.ACTIVE_PAGE_ID, new PageSettings(Localization.getString("ui.action.pad.settings.page.active"), ModernColor.GRAY1), null, null);

	private static final String PAD_GRID_BUTTON_STYLE_CLASS = "settings--pad-grid-button";

	@FXML
	private ComboBox<PadAction.PadActionMode> modeComboBox;

	@FXML
	private ComboBox<Page> pageComboBox;

	@FXML
	private GridPane padGrid;

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
		return new PadAction(PadAction.PadActionMode.PLAY_STOP, null, null);
	}

	@Override
	public void initSettings(Action action)
	{
		if(action instanceof PadAction padAction)
		{
			modeComboBox.getSelectionModel().select(padAction.getPadActionMode());

			final List<Page> pages = new ArrayList<>(projectController.getProject().getPages());
			pages.addFirst(ACTIVE_PAGE);
			pageComboBox.getItems().setAll(pages);

			if(padAction.getPageId() == null)
			{
				pageComboBox.getSelectionModel().select(ACTIVE_PAGE);
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
					// page no longer exists, fallback to all pages
					pageComboBox.getSelectionModel().select(ACTIVE_PAGE);
				}
			}

			final ProjectMetadata metadata = projectController.getProject().getMetadata();
			initializePadGrid(metadata.getNumberOfHorizontalPads(), metadata.getNumberOfVerticalPads());

			if(padAction.getPosition() != null)
			{
				padGrid.getChildren().stream()
						.filter(node -> node.getUserData().equals(padAction.getPosition() + 1))
						.findFirst()
						.ifPresent(node -> node.pseudoClassStateChanged(PseudoClasses.SELECTED, true));
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

			padGrid.getChildren().stream()
					.filter(node -> node.getPseudoClassStates().contains(PseudoClasses.SELECTED))
					.findFirst()
					.ifPresentOrElse(
							node -> padAction.setPosition((Integer) node.getUserData() - 1),
							() -> padAction.setPosition(null));
		}
	}

	private void initializePadGrid(int columns, int rows)
	{
		padGrid.getChildren().clear();

		int index = 1;
		for(int y = 0; y < rows; y++)
		{
			for(int x = 0; x < columns; x++)
			{
				final Button button = new Button(String.valueOf(index));
				button.getStyleClass().add(PAD_GRID_BUTTON_STYLE_CLASS);
				button.setUserData(index);
				button.setOnAction(_ -> selectPadButton(button));
				padGrid.add(button, x, y);
				index++;
			}
		}
	}

	private void selectPadButton(Button selectedButton)
	{
		padGrid.getChildren().forEach(node -> node.pseudoClassStateChanged(PseudoClasses.SELECTED, false));
		selectedButton.pseudoClassStateChanged(PseudoClasses.SELECTED, true);
	}
}
