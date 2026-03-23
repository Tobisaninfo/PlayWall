package de.tobias.playwall.client.view.toast;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ToastType
{
	SUCCESS(FontAwesomeType.CHECK_SOLID),
	WARNING(FontAwesomeType.CIRCLE_EXCLAMATION_SOLID),
	ERROR(FontAwesomeType.CIRCLE_XMARK_SOLID),
	INFO(FontAwesomeType.INFO_SOLID);

	private final FontAwesomeType icon;
}

