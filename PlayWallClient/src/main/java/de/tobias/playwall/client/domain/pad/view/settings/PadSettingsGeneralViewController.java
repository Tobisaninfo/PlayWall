package de.tobias.playwall.client.domain.pad.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.pad.PadContent;
import de.tobias.playwall.client.domain.pad.view.settings.content.BasePadContentSettingsContainer;
import de.tobias.playwall.client.domain.pad.view.settings.content.PadContentSettingsContainerFactory;
import de.tobias.playwall.client.net.FluentClient;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Viewcontroller for the general page in the pad settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsGeneralPageView", applyToStage = false)
public class PadSettingsGeneralViewController extends BasePadSettingsViewController
{
	@FXML
	private TextField textFieldName;
	@FXML
	private VBox contentContainer;

	@FXML
	private CheckBox eofWarningTimeCheckbox;
	@FXML
	private VBox eofWarningTimeContainer;
	@FXML
	private Slider eofWarningTimeSlider;
	@FXML
	private Label eofWarningTimeLabel;

	private BasePadContentSettingsContainer<? extends PadContent> padContentSettingsContainer;

	private final PadContentSettingsContainerFactory padContentSettingsContainerFactory;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client, PadContentSettingsContainerFactory padContentSettingsContainerFactory)
	{
		super(client);
		this.padContentSettingsContainerFactory = padContentSettingsContainerFactory;
	}

	@Override
	protected void init()
	{
		super.init();

		eofWarningTimeContainer.disableProperty().bind(eofWarningTimeCheckbox.selectedProperty().not());
		eofWarningTimeLabel.textProperty().bind(
				Bindings.createStringBinding(
						() -> Localization.getString(
								Strings.UI_SETTINGS_PROJECT_WARNING_EOF_SEC,
								String.format("%.1f", eofWarningTimeSlider.getValue())
						),
						eofWarningTimeSlider.valueProperty()
				)
		);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.pad.getName());

		// dynamic content based on pad content

		if(padContentSettingsContainer != null)
		{
			contentContainer.getChildren().remove(padContentSettingsContainer);
			padContentSettingsContainer = null;
		}

		padContentSettingsContainer = padContentSettingsContainerFactory.createPadContentSettingsContainer(param.pad, param.parentDialog);
		contentContainer.getChildren().add(padContentSettingsContainer);

		eofWarningTimeCheckbox.setSelected(param.pad.getEofWarningTime() != null);
		eofWarningTimeSlider.setValue(param.pad.getEofWarningTime() != null ? param.pad.getEofWarningTime() : 0);

		if(param.getPad().getContent() != null)
		{
			this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty().and(padContentSettingsContainer.getIsValidProperty()));
		}
		else
		{
			this.isValidProperty.unbind();
			this.isValidProperty.set(true);
		}
	}

	@Override
	public void applySettings(Param param)
	{
		param.pad.setName(textFieldName.getText());

		if(padContentSettingsContainer != null)
		{
			padContentSettingsContainer.applySettings(param);
		}

		param.pad.setEofWarningTime(eofWarningTimeCheckbox.isSelected() ? eofWarningTimeSlider.getValue() : null);
	}

	@Override
	public void cleanup()
	{
		if(padContentSettingsContainer != null)
		{
			padContentSettingsContainer.cleanup();
		}
	}
}
