package de.tobias.playwall.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.text.MessageFormat;

@Slf4j
@Component
public class SystemTrayHandler
{
	private static final String ICON_PATH = "de/tobias/playwall/server/logo/icon_small.png";

	private MenuItem clientItem;

	@EventListener(ApplicationStartedEvent.class)
	public void onApplicationStarted(ApplicationStartedEvent event)
	{
		if(!SystemTray.isSupported())
		{
			log.debug("SystemTray is not supported");
			return;
		}

		final PopupMenu popup = new PopupMenu();

		final MenuItem aboutItem = new MenuItem("PlayWall Server v8.0.0");
		popup.add(aboutItem);

		popup.addSeparator();

		clientItem = new MenuItem("0 Verbundene Clients");
		popup.add(clientItem);

		popup.addSeparator();

		final MenuItem exitItem = new MenuItem("Beenden");
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

	public void setNumberOfConnectedClients(int numberOfClients)
	{
		clientItem.setLabel(MessageFormat.format("{0} Verbundene Clients", numberOfClients));
	}
}
