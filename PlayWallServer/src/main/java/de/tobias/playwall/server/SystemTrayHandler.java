package de.tobias.playwall.server;

import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.util.OS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemTrayHandler
{
	private static final String ICON_PATH = "de/tobias/playwall/server/logo/icon_small.png";

	private Menu clientMenu;

	private final BuildProperties buildProperties;
	private final MessageSource messageSource;

	@EventListener(ApplicationStartedEvent.class)
	public void onApplicationStarted(ApplicationStartedEvent event)
	{
		if(!SystemTray.isSupported())
		{
			log.debug("SystemTray is not supported");
			return;
		}
		if(OS.isMacOS())
		{
			NativeApplication.sharedInstance().setDockIconHidden(true);
		}

		final PopupMenu popup = new PopupMenu();

		final MenuItem aboutItem = new MenuItem(getMessage("system.tray.about", buildProperties.getVersion()));
		popup.add(aboutItem);

		popup.addSeparator();

		clientMenu = new Menu(getMessage("system.tray.clients", 0));
		popup.add(clientMenu);

		popup.addSeparator();

		final MenuItem exitItem = new MenuItem(getMessage("system.tray.exit"));
		exitItem.addActionListener(e -> System.exit(0));
		popup.add(exitItem);

		final Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(ICON_PATH));
		final TrayIcon trayIcon = new TrayIcon(image);
		trayIcon.setImageAutoSize(true);
		trayIcon.setPopupMenu(popup);

		final SystemTray tray = SystemTray.getSystemTray();
		try
		{
			tray.add(trayIcon);
		}
		catch(AWTException e)
		{
			log.error("TrayIcon could not be added.", e);
		}
	}

	public void updateConnectedClients(List<String> clientAddresses)
	{
		if(clientMenu == null)
		{
			return;
		}

		clientMenu.setLabel(getMessage("system.tray.clients", clientAddresses.size()));
		clientMenu.removeAll();

		for(String clientAddress : clientAddresses)
		{
			clientMenu.add(new MenuItem(clientAddress));
		}
	}

	private String getMessage(String key, Object... args)
	{
		return messageSource.getMessage(key, args, Locale.getDefault());
	}
}
