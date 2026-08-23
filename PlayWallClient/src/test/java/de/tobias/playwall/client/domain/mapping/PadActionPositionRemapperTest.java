package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.tobias.playwall.client.domain.mapping.action.PadAction;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PadActionPositionRemapperTest
{
	private final Map<UUID, Mapping> mappings = new HashMap<>();

	private PadAction addPadAction(Mapping mapping, KeyCode keyCode, Integer position)
	{
		final PadAction padAction = new PadAction(PadAction.PadActionMode.PLAY_STOP, UUID.randomUUID(), position);
		mapping.addInputKeyWithAction(new KeyboardInputKey(keyCode, keyCode.getName()), padAction);
		return padAction;
	}

	@Test
	void testShrinkColumns()
	{
		final Mapping mapping = new Mapping();
		final PadAction firstRow = addPadAction(mapping, KeyCode.A, 2);
		final PadAction shiftedLeft = addPadAction(mapping, KeyCode.B, 4);
		final PadAction deleted = addPadAction(mapping, KeyCode.C, 7);

		mappings.put(UUID.randomUUID(), mapping);
		PadActionPositionRemapper.remapPositions(mappings, 4, 3, 2);

		assertThat(firstRow.getPosition()).isEqualTo(2);
		assertThat(shiftedLeft.getPosition()).isEqualTo(3);
		assertThat(deleted.getPosition()).isNull();
	}

	@Test
	void testGrowColumns()
	{
		final Mapping mapping = new Mapping();
		final PadAction padAction = addPadAction(mapping, KeyCode.A, 3);

		mappings.put(UUID.randomUUID(), mapping);
		PadActionPositionRemapper.remapPositions(mappings, 3, 4, 2);

		assertThat(padAction.getPosition()).isEqualTo(4);
	}

	@Test
	void testRemoveBottomRow()
	{
		final Mapping mapping = new Mapping();
		final PadAction kept = addPadAction(mapping, KeyCode.A, 6);
		final PadAction deleted = addPadAction(mapping, KeyCode.B, 8);

		mappings.put(UUID.randomUUID(), mapping);
		PadActionPositionRemapper.remapPositions(mappings, 4, 4, 2);

		assertThat(kept.getPosition()).isEqualTo(6);
		assertThat(deleted.getPosition()).isNull();
	}

	@Test
	void testShrinkRowsAndColumns()
	{
		final Mapping mapping = new Mapping();
		final PadAction topLeft = addPadAction(mapping, KeyCode.A, 0);
		final PadAction inner = addPadAction(mapping, KeyCode.B, 5);
		final PadAction removedColumn = addPadAction(mapping, KeyCode.C, 7);
		final PadAction removedRow = addPadAction(mapping, KeyCode.D, 9);

		mappings.put(UUID.randomUUID(), mapping);
		PadActionPositionRemapper.remapPositions(mappings, 4, 2, 2);

		assertThat(topLeft.getPosition()).isZero();
		assertThat(inner.getPosition()).isEqualTo(3);
		assertThat(removedColumn.getPosition()).isNull();
		assertThat(removedRow.getPosition()).isNull();
	}

	@Test
	void testSameDimensionsKeepsPositions()
	{
		final Mapping mapping = new Mapping();
		final PadAction padAction = addPadAction(mapping, KeyCode.A, 5);

		mappings.put(UUID.randomUUID(), mapping);
		PadActionPositionRemapper.remapPositions(mappings, 4, 4, 2);

		assertThat(padAction.getPosition()).isEqualTo(5);
	}

	@Test
	void testNullPositionStaysNull()
	{
		final Mapping mapping = new Mapping();
		final PadAction padAction = addPadAction(mapping, KeyCode.A, null);

		mappings.put(UUID.randomUUID(), mapping);
		PadActionPositionRemapper.remapPositions(mappings, 4, 2, 2);

		assertThat(padAction.getPosition()).isNull();
	}

	@Test
	void testAllMappingsAreRemapped()
	{
		final Mapping firstMapping = new Mapping();
		final Mapping secondMapping = new Mapping();
		final PadAction first = addPadAction(firstMapping, KeyCode.A, 4);
		final PadAction second = addPadAction(secondMapping, KeyCode.A, 4);

		mappings.put(UUID.randomUUID(), firstMapping);
		mappings.put(UUID.randomUUID(), secondMapping);
		PadActionPositionRemapper.remapPositions(mappings, 4, 3, 2);

		assertThat(List.of(first.getPosition(), second.getPosition())).containsExactly(3, 3);
	}
}
