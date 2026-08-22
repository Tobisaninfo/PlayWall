package de.tobias.playwall.client.domain.midi.feedback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeedbackValueDescription
{
	Class<? extends FeedbackValueSettingsViewController> settingsViewController();
}
