package de.tobias.playwall.client.extensions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LoggerSetup implements BeforeAllCallback
{
	@Override
	public void beforeAll(ExtensionContext context)
	{
		System.setProperty("app.debug", "true");
	}
}
