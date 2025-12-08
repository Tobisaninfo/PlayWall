package de.tobias.playwall.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemTrayHandler
{
	private static final String ICON_PATH = "de/tobias/playwall/server/logo/icon_small.png";

	private Menu clientMenu;

	private final AppInfo appInfo;

	@EventListener(ApplicationStartedEvent.class)
	public void onApplicationStarted(ApplicationStartedEvent event)
	{
		if(!SystemTray.isSupported())
		{
			log.debug("SystemTray is not supported");
			return;
		}

		final PopupMenu popup = new PopupMenu();

		final MenuItem aboutItem = new MenuItem(MessageFormat.format("PlayWall Server v{0}", appInfo.getVersion()));
		popup.add(aboutItem);

		popup.addSeparator();

		clientMenu = new Menu("0 Verbundene Clients");
		popup.add(clientMenu);

		popup.addSeparator();

		final MenuItem exitItem = new MenuItem("Beenden");
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

		clientMenu.setLabel(MessageFormat.format("{0} Verbundene Clients", clientAddresses.size()));
		clientMenu.removeAll();

		for(String clientAddress : clientAddresses)
		{
			clientMenu.add(new MenuItem(clientAddress));
		}
	}
}
