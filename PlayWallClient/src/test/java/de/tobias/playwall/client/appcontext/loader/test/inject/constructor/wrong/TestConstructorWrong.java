package de.tobias.playwall.client.appcontext.loader.test.inject.constructor.wrong;

import de.tobias.playwall.client.appcontext.Service;

@Service
public class TestConstructorWrong
{
	public static class Dependency
	{

	}

	public TestConstructorWrong(Dependency dependency)
	{
	}
}
