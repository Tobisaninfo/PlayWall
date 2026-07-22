package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageMapper;
import de.tobias.playwall.client.domain.page.PageSettingsMapper;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.ColorMapper;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.page.update.*;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PageListener
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;
	private final PageMapper pageMapper;
	private final PageSettingsMapper pageSettingsMapper;

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

	@EventListener(PageInsertUpdate.class)
	void onPageInsertUpdate(PageInsertUpdate message)
	{
		final Page page = pageMapper.pageDtoToPage(message.getPage());
		projectController.insertPage(page, message.getIndex(), message.getPositions());

		Platform.runLater(() -> {
			mainViewController.buildPageButtons();
			mainViewController.showPage(page);
		});
	}

	@EventListener(PageReplaceUpdate.class)
	void onPageReplaceUpdate(PageReplaceUpdate message)
	{
		final Page page = pageMapper.pageDtoToPage(message.getPage());
		projectController.replacePage(page, message.getIndex());

		Platform.runLater(() -> {
			mainViewController.buildPageButtons();
			mainViewController.showPage(page);
		});
	}

	@EventListener(PageReorderUpdate.class)
	void onPageReorderUpdate(PageReorderUpdate update)
	{
		projectController.updatePagePositions(update.getPositions());
		Platform.runLater(mainViewController::buildPageButtons);
	}

	@EventListener(PageSettingsUpdate.class)
	void onPageSettingsUpdate(PageSettingsUpdate update)
	{
		projectController.updatePageSettings(update.getPageId(), pageSettingsMapper.pageSettingsDtoToPageSettings(update.getPageSettings()));
		Platform.runLater(mainViewController::buildPageButtons);
	}

	@EventListener(PageDeleteUpdate.class)
	void onPageDeleteUpdate(PageDeleteUpdate message)
	{
		projectController.deletePage(message.getPageId(), message.getPositions());

		Platform.runLater(() -> {
			mainViewController.buildPageButtons();

			if(mainViewController.getCurrentPage().getId().equals(message.getPageId()))
			{
				mainViewController.showPage(0);
			}
		});
	}
}
