package de.tobias.playwall.server.common.util.condition;

import de.thecodelabs.utils.util.OS;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OperatingSystemConditions
{
	public static class WindowsCondition implements Condition
	{
		@Override
		public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata)
		{
			return OS.isWindows();
		}
	}

	public static class MacOSCondition implements Condition
	{
		@Override
		public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata)
		{
			return OS.isMacOS();
		}
	}
}
