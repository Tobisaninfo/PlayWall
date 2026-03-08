package de.tobias.playwall.server.common.model.project;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecentProjectsTest
{
	@Test
	void testAdd()
	{
		final RecentProjectsStack recentProjectsStack = new RecentProjectsStack();
		recentProjectsStack.push(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));

		assertThat(recentProjectsStack)
				.hasSize(1)
				.containsExactly(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
	}

	@Test
	void testAddMultiple()
	{
		final RecentProjectsStack recentProjectsStack = new RecentProjectsStack();
		recentProjectsStack.push(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
		recentProjectsStack.push(UUID.fromString("d3d36465-7f50-4143-982d-cf5c56f5dbd9"));

		assertThat(recentProjectsStack).hasSize(2)
				.containsExactly(UUID.fromString("d3d36465-7f50-4143-982d-cf5c56f5dbd9"),
						UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
	}

	@Test
	void testAddMoreThanMaxSize()
	{
		final RecentProjectsStack recentProjectsStack = new RecentProjectsStack();
		recentProjectsStack.push(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
		recentProjectsStack.push(UUID.fromString("d3d36465-7f50-4143-982d-cf5c56f5dbd9"));
		recentProjectsStack.push(UUID.fromString("a3d82db5-e27e-41d4-acb5-4ff6d5d49a29"));
		recentProjectsStack.push(UUID.fromString("0a010584-dd72-432d-a82b-46ebdd961424"));
		recentProjectsStack.push(UUID.fromString("e63588ba-f450-44d5-9181-24abc5b4bd30"));
		recentProjectsStack.push(UUID.fromString("6471e951-bf90-423f-a2ea-be52d723d298"));

		assertThat(recentProjectsStack).hasSize(5)
				.containsExactly(
						UUID.fromString("6471e951-bf90-423f-a2ea-be52d723d298"),
						UUID.fromString("e63588ba-f450-44d5-9181-24abc5b4bd30"),
						UUID.fromString("0a010584-dd72-432d-a82b-46ebdd961424"),
						UUID.fromString("a3d82db5-e27e-41d4-acb5-4ff6d5d49a29"),
						UUID.fromString("d3d36465-7f50-4143-982d-cf5c56f5dbd9")
				);
	}

	@Test
	void testAddSameTwice()
	{
		final RecentProjectsStack recentProjectsStack = new RecentProjectsStack();
		recentProjectsStack.push(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
		recentProjectsStack.push(UUID.fromString("d3d36465-7f50-4143-982d-cf5c56f5dbd9"));
		recentProjectsStack.push(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));

		assertThat(recentProjectsStack).hasSize(2)
				.containsExactly(
						UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"),
						UUID.fromString("d3d36465-7f50-4143-982d-cf5c56f5dbd9")
				);
	}
}
