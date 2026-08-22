package de.tobias.playwall.client.domain.mapping.action;

import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionDescription
{
	String nameKey();

	DefaultFeedbackState[] feedbackTypes() default {};

	int order();

	Class<? extends ActionSettingsViewController> settingsViewController();
}
