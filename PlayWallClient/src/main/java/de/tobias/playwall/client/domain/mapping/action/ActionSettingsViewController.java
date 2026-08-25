package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.feedback.FeedbackProvider;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.utils.ui.NVC;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.midi.MidiCoordinator;
import de.tobias.playwall.client.domain.midi.device.CustomMidiDevice;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueDescription;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueSettingsViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base view controller for the settings view of an action of the mapping. It contains customization for the action.
 */
public abstract class ActionSettingsViewController extends NVC
{
	@FXML
	private VBox feedbackValueContainer;

	private List<FeedbackValueSettingsViewController> feedbackValueSettingsViewControllers;

	/**
	 * Create a new instance of the action with default settings. If possible, no uninitialized values.
	 *
	 * @return new action
	 */
	public abstract Action createNewAction();

	/**
	 * Set the action settings values to the view components
	 *
	 * @param action action to set the settings for
	 */
	public abstract void initSettings(Action action);

	/**
	 * Apply the settings from the view components to the action to persist
	 *
	 * @param action action to apply the settings to
	 */
	public abstract void applySettings(Action action);

	public void createFeedbackValueViews(InputKey key, List<? extends FeedbackState> feedbackStates, MidiCoordinator midiCoordinator)
	{
		feedbackValueContainer.getChildren().clear();
		feedbackValueSettingsViewControllers = new LinkedList<>();

		if(!(key instanceof FeedbackProvider feedbackProvider))
		{
			return;
		}
		final Optional<CustomMidiDevice> customMidiDeviceOptional = midiCoordinator.lookupCustomDevice();
		if(customMidiDeviceOptional.isEmpty())
		{
			return;
		}
		final CustomMidiDevice customMidiDevice = customMidiDeviceOptional.get();
		for(FeedbackState feedbackState : feedbackStates)
		{
			final FeedbackValueSettingsViewController viewController = createFeedbackValueSettingsViewController(customMidiDevice.supportedFeedbackValue());
			final FeedbackValue feedbackValue = Optional.ofNullable(feedbackProvider.getFeedbackValueForState(feedbackState)).orElseGet(() -> {
				// Create new feedback value if not existing for the state
				final FeedbackValue newFeedbackValue = viewController.createNewFeedback();
				feedbackProvider.setFeedbackValueForState(feedbackState, newFeedbackValue);
				return newFeedbackValue;
			});
			viewController.initSettings(feedbackState, feedbackValue);

			feedbackValueContainer.getChildren().add(viewController.getParent());
			feedbackValueSettingsViewControllers.add(viewController);
		}

		if(!feedbackValueContainer.getChildren().isEmpty())
		{
			feedbackValueContainer.getChildren().addFirst(new Separator());

			final Label label = new Label(customMidiDevice.getName());
			label.getStyleClass().add("sub-headline");
			feedbackValueContainer.getChildren().add(1, label);
		}
	}

	private FeedbackValueSettingsViewController createFeedbackValueSettingsViewController(Class<? extends FeedbackValue> clazz)
	{
		final FeedbackValueDescription description = clazz.getAnnotation(FeedbackValueDescription.class);
		return AppContextHolder.getInstance().get(description.settingsViewController());
	}

	public void applySettingsForFeedbackValues(InputKey key, List<? extends FeedbackState> feedbackStates)
	{
		if(!(key instanceof FeedbackProvider feedbackProvider))
		{
			return;
		}
		for(int i = 0; i < Math.min(feedbackStates.size(), feedbackValueSettingsViewControllers.size()); i++)
		{
			final FeedbackState feedbackState = feedbackStates.get(i);
			final FeedbackValueSettingsViewController viewController = feedbackValueSettingsViewControllers.get(i);
			final FeedbackValue feedbackValue = Optional.ofNullable(feedbackProvider.getFeedbackValueForState(feedbackState)).orElseThrow();
			viewController.applySettings(feedbackValue);
		}
	}
}
