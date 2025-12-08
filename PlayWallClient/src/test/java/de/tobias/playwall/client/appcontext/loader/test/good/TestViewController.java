package de.tobias.playwall.client.appcontext.loader.test.good;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.appcontext.ViewController;
import lombok.Getter;

@ViewController(path = "", view = "")
@Getter
public class TestViewController
{
	private final TestSingleton singleton;

	@InjectField
	private TestInterface anInterface;

	@InjectConstructor
	public TestViewController(TestSingleton singleton)
	{
		this.singleton = singleton;
	}
}
