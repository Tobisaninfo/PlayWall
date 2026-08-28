package de.tobias.playwall.client.domain.project.view.settings.mapping;

import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.page.update.*;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ProjectSettingsMappingPageListener
{
	private final ProjectSettingsMappingViewController controller;

	@EventListener(PageSettingsUpdate.class)
	void onPageRename(PageSettingsUpdate event)
	{
		Platform.runLater(controller::updateInputListView);
	}

	@EventListener(PageAddUpdate.class)
	void onPageAdd(PageAddUpdate event)
	{
		Platform.runLater(controller::updateInputListView);
	}

	@EventListener(PageDeleteUpdate.class)
	void onPageDelete(PageDeleteUpdate event)
	{
		Platform.runLater(controller::updateInputListView);
	}

	@EventListener(PageInsertUpdate.class)
	void onPageInsert(PageInsertUpdate event)
	{
		Platform.runLater(controller::updateInputListView);
	}

	@EventListener(PageReplaceUpdate.class)
	void onPageReplace(PageReplaceUpdate event)
	{
		Platform.runLater(controller::updateInputListView);
	}

	@EventListener(PageReorderUpdate.class)
	void onPageReorder(PageReorderUpdate event)
	{
		Platform.runLater(controller::updateInputListView);
	}
}
