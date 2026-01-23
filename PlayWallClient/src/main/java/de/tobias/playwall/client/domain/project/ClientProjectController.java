package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.page.Page;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
public class ClientProjectController
{
	private final Map<UUID, ClientPadController> padControllers = new HashMap<>();
	private Project project;

	public void loadProject(Project project)
	{
		this.project = project;
		this.padControllers.clear();

		for(Page page : project.getPages())
		{
			createPadControllerForPage(page);
		}
	}

	public ClientPadController getPadController(UUID uuid)
	{
		return padControllers.get(uuid);
	}

	private void createPadController(Pad pad)
	{
		final ClientPadController controller = new ClientPadController(pad, getProject().getMetadata());
		padControllers.put(pad.getId(), controller);
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

	private void createPadControllerForPage(Page page)
	{
		for(Pad pad : page.getPads())
		{
			createPadController(pad);
		}
	}
}
