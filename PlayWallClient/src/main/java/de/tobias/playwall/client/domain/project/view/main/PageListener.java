package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageMapper;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PageListener
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;
	private final PageMapper pageMapper;

	@EventListener(PageAddUpdate.class)
	void onPageAddUpdate(PageAddUpdate message)
	{
		final Page page = pageMapper.pageDtoToPage(message.getPage());
		projectController.addPage(page);
		Platform.runLater(() -> {
			mainViewController.buildPageButtons();
			mainViewController.showPage(page);
		});
	}
}
