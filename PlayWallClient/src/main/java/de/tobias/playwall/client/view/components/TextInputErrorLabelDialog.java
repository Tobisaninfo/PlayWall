package de.tobias.playwall.client.view.components;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class TextInputErrorLabelDialog extends TextInputDialog
{
	public interface Validator
	{
		/**
		 * Validate the user input and return error label text, if user input is invalid.
		 *
		 * @param input current user input
		 * @return human-readable error message, if input is invalid, otherwise null
		 */
		String validate(String input);
	}

	private final Validator validator;

	public TextInputErrorLabelDialog(String initial, Validator validator)
	{
		this(validator);
		getEditor().setText(initial);
	}

	public TextInputErrorLabelDialog(Validator validator)
	{
		this.validator = validator;
	}

	public void createErrorLabel()
	{
		final TextField textField = getEditor();

		final Label errorLabel = new Label();
		errorLabel.getStyleClass().add("error-label");
		errorLabel.setVisible(false);
		errorLabel.setPadding(new Insets(10, 0, 0, 0));

		final GridPane content = (GridPane) this.getDialogPane().getContent();
		content.add(errorLabel, 1, 1);

		final Button okButton = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
		okButton.addEventFilter(ActionEvent.ACTION, event -> {
			final String newName = textField.getText().trim();

			final String errorMessage = validator.validate(newName);
			if(errorMessage != null)
			{
				errorLabel.setText(errorMessage);
				errorLabel.setVisible(true);
				event.consume();
				return;
			}

			errorLabel.setVisible(false);
		});
	}


}
