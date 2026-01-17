package de.tobias.playwall.server.history;

import de.tobias.playwall.common.api.history.UndoHistoryUpdate;
import de.tobias.playwall.common.api.project.PadDeleteContentRequest;
import de.tobias.playwall.common.api.project.PadNewMediaRequest;
import de.tobias.playwall.common.api.project.PadSettingsUpdateRequest;
import de.tobias.playwall.common.net.RequestMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class UndoManagerTest
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private UndoManager manager;

	@BeforeEach
	void clear()
	{
		manager.clear();
	}

	/*
	History: []
	Operations: Undo
	 */
	@Test
	void testUndoWithNoItemsInHistory()
	{
		final RequestMessage undoOperation = manager.getUndoOperation();
		assertThat(undoOperation).isNull();
	}

	/*
	History: [Demo]
	Operations: Undo
	 */
	@Test
	void testUndoWithOneItemInHistory()
	{
		manager.addUndoOperation(new UndoItem("Demo", new PadNewMediaRequest(null, null), new PadDeleteContentRequest(null)));
		applicationEvents.clear();

		final RequestMessage undoOperation1 = manager.getUndoOperation();
		assertThat(undoOperation1).isInstanceOf(PadDeleteContentRequest.class);

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isNull())
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isEqualTo("Demo"));

		final RequestMessage undoOperation2 = manager.getUndoOperation();
		assertThat(undoOperation2).isNull();
	}

	/*
	History: [Demo 1, Demo 2]
	Operations: Undo, Undo
	 */
	@Test
	void testUndoWithTwoItemInHistory()
	{
		manager.addUndoOperation(new UndoItem("Demo 1", new PadNewMediaRequest(null, null), new PadDeleteContentRequest(null)));
		manager.addUndoOperation(new UndoItem("Demo 2", new PadSettingsUpdateRequest(null, null), new PadSettingsUpdateRequest(null, null)));
		applicationEvents.clear();

		final RequestMessage undoOperation1 = manager.getUndoOperation();
		assertThat(undoOperation1).isInstanceOf(PadSettingsUpdateRequest.class);

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isEqualTo("Demo 1"))
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isEqualTo("Demo 2"));

		final RequestMessage undoOperation2 = manager.getUndoOperation();
		assertThat(undoOperation2).isInstanceOf(PadDeleteContentRequest.class);

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isNull())
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isEqualTo("Demo 1"));

		final RequestMessage undoOperation3 = manager.getUndoOperation();
		assertThat(undoOperation3).isNull();
	}

	/*
	History: []
	Operations: Redo
	 */
	@Test
	void testRedoWithNoItemsInHistory()
	{
		final RequestMessage redoOperation = manager.getRedoOperation();
		assertThat(redoOperation).isNull();
	}

	/*
	History: [Demo 1]
	Operations: Redo
	 */
	@Test
	void testRedoWithItemsInHistoryWithoutAnyUndoFirst()
	{
		manager.addUndoOperation(new UndoItem("Demo 1", new PadNewMediaRequest(null, null), new PadDeleteContentRequest(null)));
		manager.addUndoOperation(new UndoItem("Demo 2", new PadSettingsUpdateRequest(null, null), new PadSettingsUpdateRequest(null, null)));

		final RequestMessage redoOperation = manager.getRedoOperation();
		assertThat(redoOperation).isNull();
	}

	/*
	History: [Demo 1, Demo 2]
	Operations: Undo, Undo, Redo, Redo
	 */
	@Test
	void testRedoWithTwoItemInHistory()
	{
		manager.addUndoOperation(new UndoItem("Demo 1", new PadNewMediaRequest(null, null), new PadDeleteContentRequest(null)));
		manager.addUndoOperation(new UndoItem("Demo 2", new PadSettingsUpdateRequest(null, null), new PadSettingsUpdateRequest(null, null)));
		applicationEvents.clear();

		manager.getUndoOperation();
		manager.getUndoOperation();

		final RequestMessage redoOperation1 = manager.getRedoOperation();
		assertThat(redoOperation1).isInstanceOf(PadNewMediaRequest.class);

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isEqualTo("Demo 1"))
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isEqualTo("Demo 2"));

		final RequestMessage redoOperation2 = manager.getRedoOperation();
		assertThat(redoOperation2).isInstanceOf(PadSettingsUpdateRequest.class);

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isEqualTo("Demo 2"))
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isNull());

		final RequestMessage redoOperation3 = manager.getRedoOperation();
		assertThat(redoOperation3).isNull();
	}

	/*
	History: [Demo 1, Demo 2]
	Operations: Undo, Undo, Add Operation, Redo
	 */
	@Test
	void testRedoWithAddNewItemInStackFirst()
	{
		manager.addUndoOperation(new UndoItem("Demo 1", new PadNewMediaRequest(null, null), new PadDeleteContentRequest(null)));
		manager.addUndoOperation(new UndoItem("Demo 2", new PadSettingsUpdateRequest(null, null), new PadSettingsUpdateRequest(null, null)));
		applicationEvents.clear();

		manager.getUndoOperation();
		manager.getUndoOperation();

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isNull())
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isEqualTo("Demo 1"));

		manager.addUndoOperation(new UndoItem("New Work", new PadSettingsUpdateRequest(null, null), new PadSettingsUpdateRequest(null, null)));

		assertThat(applicationEvents.stream(UndoHistoryUpdate.class))
				.last()
				.satisfies(item -> assertThat(item.getNextUndoOperation()).isEqualTo("New Work"))
				.satisfies(item -> assertThat(item.getNextRedoOperation()).isNull());

		final RequestMessage redoOperation = manager.getRedoOperation();
		assertThat(redoOperation).isNull();
	}
}
