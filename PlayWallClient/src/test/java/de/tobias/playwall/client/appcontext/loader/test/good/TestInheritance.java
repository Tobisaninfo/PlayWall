package de.tobias.playwall.client.appcontext.loader.test.good;

import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.appcontext.Service;

@SuppressWarnings("unused")
@Service(singleton = false, superclass = TestInterface.class)
public class TestInheritance implements TestInterface
{
	@InjectField
	private TestSingleton singleton;
}
