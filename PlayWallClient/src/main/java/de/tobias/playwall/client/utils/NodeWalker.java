package de.tobias.playwall.client.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class NodeWalker
{
	public static List<Node> getAllNodes(Parent root)
	{
		final List<Node> nodes = new ArrayList<>();
		nodes.add(root);
		addAllDescendents(root, nodes);
		return nodes;
	}

	private static void addAllDescendents(Parent parent, List<Node> nodes)
	{
		for(Node node : parent.getChildrenUnmodifiable())
		{
			nodes.add(node);

			if(node instanceof Labeled labeled)
			{
				final Node graphic = labeled.getGraphic();
				if(graphic != null)
				{
					nodes.add(graphic);
				}
			}

			if(node instanceof Parent child)
				addAllDescendents(child, nodes);
		}
	}
}
