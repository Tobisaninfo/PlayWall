package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageSettings;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Service
@Getter
public class ClientProjectController
{
	private final Map<UUID, ClientPadController> padControllers = new HashMap<>();
	private Project project;

	@Setter
	private int currentPageIndex;

	public Page getCurrentPage()
	{
		return project.getPage(currentPageIndex);
	}

	public void loadProject(Project project)
	{
		this.project = project;
		this.padControllers.clear();

		for(Page page : project.getPages())
		{
			createPadControllerForPage(page);
		}
	}

	public void updateProject(Project project)
	{
		this.project = project;

		final Map<UUID, ClientPadController> newPadControllers = new HashMap<>();
		for(Page page : project.getPages())
		{
			for(Pad pad : page.getPads())
			{
				final ClientPadController padController = getPadController(pad.getId());
				if(padController == null)
				{
					final ClientPadController controller = new ClientPadController(pad, getProject().getMetadata());
					newPadControllers.put(pad.getId(), controller);
				}
				else
				{
					padController.setPad(pad);
					newPadControllers.put(pad.getId(), padController);
				}
			}
		}

		padControllers.clear();
		padControllers.putAll(newPadControllers);
	}

	public ClientPadController getPadController(UUID uuid)
	{
		return padControllers.get(uuid);
	}

	public ClientPadController updatePad(Pad newPad)
	{
		for(Page page : project.getPages())
		{
			for(int i = 0; i < page.getPads().size(); i++)
			{
				if(page.getPads().get(i).getId().equals(newPad.getId()))
				{
					page.getPads().set(i, newPad);

					final ClientPadController controller = getPadController(newPad.getId());
					controller.setPad(newPad);
					return controller;
				}
			}
		}
		return null;
	}

	public void updateMetadata(ProjectMetadata projectMetadata)
	{
		getProject().setMetadata(projectMetadata);

		for(ClientPadController padController : padControllers.values())
		{
			padController.setProjectMetadata(projectMetadata);
		}
	}

	public void addPage(Page page)
	{
		project.getPages().add(page);
		createPadControllerForPage(page);
	}

	public void insertPage(Page page, int index, Map<UUID, Integer> positions)
	{
		project.getPages().add(index, page);
		createPadControllerForPage(page);
		updatePagePositions(positions);
	}

	public void replacePage(Page page, int index)
	{
		// TODO Remove old pad controllers
		project.getPages().set(index, page);
		createPadControllerForPage(page);
	}

	public void deletePage(UUID pageId, Map<UUID, Integer> positions)
	{
		// Remove the old pad controllers
		final Page page = project.getPage(pageId);
		page.getPads().forEach(pad -> padControllers.remove(pad.getId()));

		// Remove page
		project.getPages().remove(page);

		// Update remaining page positions
		positions.forEach((id, position) -> project.getPage(id).setPosition(position));
	}

	public void updatePagePositions(Map<UUID, Integer> positions)
	{
		positions.forEach((id, position) -> project.getPage(id).setPosition(position));
		project.getPages().sort(Comparator.comparing(Page::getPosition));
	}


	public void updatePageSettings(UUID pageId, PageSettings pageSettings)
	{
		final Page page = project.getPage(pageId);
		page.setSettings(pageSettings);
	}

	private void createPadControllerForPage(Page page)
	{
		for(Pad pad : page.getPads())
		{
			createPadController(pad);
		}
	}

	public void createPadController(Pad pad)
	{
		final ClientPadController controller = new ClientPadController(pad, getProject().getMetadata());
		padControllers.put(pad.getId(), controller);
	}

	public void removePadController(UUID padId)
	{
		padControllers.remove(padId);
	}

	public void replacePad(Pad source, UUID target)
	{
		removePadController(target);

		final Pad targetPad = project.getPad(target);
		final Page targetPage = project.getPageByPadId(target);
		targetPage.replacePad(source, targetPad);

		createPadController(source);
	}

	public void swapPads(UUID padId1, UUID padId2)
	{
		final Pad pad1 = project.getPad(padId1);
		final Pad pad2 = project.getPad(padId2);

		final Page pad1Page = project.getPageByPadId(padId1);
		final Page pad2Page = project.getPageByPadId(padId2);

		pad1Page.removePad(pad1);
		pad2Page.removePad(pad2);

		int position = pad1.getPosition();
		pad1.setPosition(pad2.getPosition());
		pad2.setPosition(position);

		pad1Page.insertPad(pad2);
		pad2Page.insertPad(pad1);
	}

	public boolean isAtLeastOnePadPlaying()
	{
		return padControllers.values().stream()
				.anyMatch(padController -> padController.getStatus() == PadStatus.PLAYING);
	}

	public List<ClientPadController> getPadControllersWithState(PadStatus status)
	{
		return padControllers.values().stream()
				.filter(padController -> padController.getStatus() == status)
				.toList();
	}
}
