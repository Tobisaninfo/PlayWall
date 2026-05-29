package de.tobias.playwall.client.view.components;

import de.tobias.playwall.client.utils.ChildExpandable;
import de.tobias.playwall.client.view.validation.Validator;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.Getter;

import static de.tobias.playwall.client.view.components.PseudoClasses.ERROR_CLASS;

public class ValidatedTextField extends VBox implements ChildExpandable
{
	@Getter
	private final TextField textField = new TextField();
	private final Label errorLabel = new Label();
	private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(true);

	@Getter
	private Validator validator;

	public ValidatedTextField()
	{
		textField.setMaxWidth(Double.MAX_VALUE);
		textField.prefWidthProperty().bind(prefWidthProperty());

		errorLabel.getStyleClass().add("error-label");
		errorLabel.prefWidthProperty().bind(prefWidthProperty());

		getChildren().addAll(textField, errorLabel);

		textField.textProperty().addListener((_, oldValue, newValue) -> applyValidation(oldValue, newValue));
	}

	private void applyValidation(String oldValue, String newValue)
	{
		if(validator == null)
		{
			setErrorState(null);
			return;
		}

		final String errorMessage = validator.validate(newValue);

		final boolean hasError = errorMessage != null;
		valid.set(!hasError);

		if(oldValue != null)
		{
			setErrorState(errorMessage);
		}
	}

	private void setErrorState(String errorMessage)
	{
		final boolean hasError = errorMessage != null;
		textField.pseudoClassStateChanged(ERROR_CLASS, hasError);
		errorLabel.setText(hasError ? errorMessage : "");
	}

	public void showError(String message)
	{
		setErrorState(message);
	}

	public void clearError()
	{
		setErrorState(null);
	}

	public void setValidator(Validator validator)
	{
		this.validator = validator;
		applyValidation(null, textField.getText());
	}

	public ReadOnlyBooleanProperty validProperty()
	{
		return valid.getReadOnlyProperty();
	}

	public boolean isValid()
	{
		return valid.get();
	}

	public StringProperty textProperty()
	{
		return textField.textProperty();
	}

	public String getText()
	{
		return textField.getText();
	}

	public void setText(String text)
	{
		textField.setText(text);
	}

	public StringProperty promptTextProperty()
	{
		return textField.promptTextProperty();
	}

	public String getPromptText()
	{
		return textField.getPromptText();
	}

	public void setPromptText(String text)
	{
		textField.setPromptText(text);
	}

}
