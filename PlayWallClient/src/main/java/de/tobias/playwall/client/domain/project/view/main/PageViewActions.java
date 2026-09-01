package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.view.PageButtons;
import de.tobias.playwall.client.domain.page.view.settings.BasePageSettingsViewController;
import de.tobias.playwall.client.domain.page.view.settings.PageSettingsViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.ExportFile;
import de.tobias.playwall.client.utils.MimeType;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import javafx.event.ActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.thecodelabs.utils.util.Localization.getString;

@RequiredArgsConstructor
@Slf4j
class PageViewActions
{
	private final ClientProjectController projectController;
	private final FluentClient client;
	private final ErrorAlertBuilder errorAlertBuilder;
	private final FileChooserWrapper fileChooserWrapper;
	private final MainViewController mainViewController;

	void onPageSettingsMenuItem(Page page)
	{
		final PageSettingsViewController controller = AppContextHolder.getInstance().get(PageSettingsViewController.class);
		controller.showAndWait(new BasePageSettingsViewController.Param(page), mainViewController.getContainingWindow());
	}

	void onPageDuplicateMenuItem(Page page)
	{
		try
		{
			client.currentProject().page(page.getId()).duplicate();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot duplicate page", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_DUPLICATE), e.getMessage(), e.getError(), mainViewController.getContainingWindow()).showAndWait();
		}
	}

	void onPageExportMenuItem(Page page)
	{
		try
		{
			final ExportFile export = client.currentProject().page(page.getId()).export();
			final String initialFileName = getString(Strings.UI_PAGE_EXPORT_NAME,
					projectController.getProject().getMetadata().getName(),
					page.getSettings().getName())
					.replaceAll("[^a-zA-Z0-9\\s\\-_]", "_");

			final MimeType mimeType = MimeType.getByMimeType(export.mimetype());
			fileChooserWrapper.setExtensionFilter(List.of(mimeType.toExtensionFilter()));
			fileChooserWrapper.setInitialFilename(initialFileName + "." + mimeType.getExtension());
			final Optional<Path> pathOptional = fileChooserWrapper.showSaveFile(mainViewController.getContainingWindow());
			if(pathOptional.isEmpty())
			{
				return;
			}
			final Path path = pathOptional.get();
			Files.write(path, export.data());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot export page", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_EXPORT), e.getMessage(), e.getError(), mainViewController.getContainingWindow()).showAndWait();
		}
		catch(IOException e)
		{
			log.error("Cannot write file", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_EXPORT), e.getMessage(), mainViewController.getContainingWindow()).showAndWait();
		}
	}

	void onPageDeleteMenuItem(Page page)
	{
		try
		{
			client.currentProject().page(page.getId()).delete();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot delete page", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_DELETE), e.getMessage(), e.getError(), mainViewController.getContainingWindow()).showAndWait();
		}
	}

	void onPageReorder(PageButtons.PageReorderEvent event)
	{
		try
		{
			client.currentProject().reorderPages(event.getPages().stream().collect(Collectors.toMap(Page::getId, page -> event.getPages().indexOf(page))));
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot reorder page", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_REORDER), e.getMessage(), e.getError(), mainViewController.getContainingWindow()).showAndWait();
		}
	}

	void onPageAddNew(ActionEvent event)
	{
		try
		{
			client.currentProject().addPage();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot add page", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_ADD), e.getMessage(), e.getError(), mainViewController.getContainingWindow()).showAndWait();
		}
	}

	void onPageImport(ActionEvent event)
	{
		final MimeType mimeType = MimeType.APPLICATION_JSON;
		fileChooserWrapper.setExtensionFilter(List.of(mimeType.toExtensionFilter()));
		final Optional<Path> pathOptional = fileChooserWrapper.showOpenFile(mainViewController.getContainingWindow());
		if(pathOptional.isEmpty())
		{
			return;
		}
		try
		{
			final byte[] bytes = Files.readAllBytes(pathOptional.get());
			client.currentProject().importPage(new ExportFile(mimeType.getMimeTypeValue(), bytes));
		}
		catch(IOException e)
		{
			log.error("Cannot read file", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_IMPORT), e.getMessage(), mainViewController.getContainingWindow()).showAndWait();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot import page", e);
			errorAlertBuilder.createErrorAlert(null, getString(Strings.UI_ERRORS_PAGE_IMPORT), e.getMessage(), e.getError(), mainViewController.getContainingWindow()).showAndWait();
		}
	}
}