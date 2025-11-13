package de.tobias.playwall.client.viewcontroller;

import de.tobias.playwall.client.extenions.AppEnvironmentSetup;
import de.tobias.playwall.client.utils.ScreenshotOnFailure;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(AppEnvironmentSetup.class)
@ExtendWith(ApplicationExtension.class)
@ExtendWith(ScreenshotOnFailure.class)
public abstract class AbstractViewControllerTest
{
}
