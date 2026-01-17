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
			for(Pad pad : page.getPads())
			{
				createPadController(pad);
			}
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
}
