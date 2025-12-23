package de.tobias.playwall.client.service;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;

@Service
public class ProjectService
{
	public boolean updatePad(Project project, Pad newPad)
	{
		for(Page page : project.getPages())
		{
			for(int i = 0; i < page.getPads().size(); i++)
			{
				if(page.getPads().get(i).getId().equals(newPad.getId()))
				{
					page.getPads().set(i, newPad);
					return true;
				}
			}
		}
		return false;
	}
}
