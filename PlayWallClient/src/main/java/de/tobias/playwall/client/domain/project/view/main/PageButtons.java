package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class PageButtons extends FlowPane
{
	private static final DataFormat PAGE_BUTTON_DND = new DataFormat("application/x-playwall-page-button");
	public static final String PAGE_BUTTON_DRAGGING_STYLECLASS = "page-button-dragging";

	private Button draggedPageButton;
	private int originalDraggedIndex = -1;

	private final Region dropPlaceholder = new Region();

	@Getter
	@Setter
	private boolean isLoading = false;

	public PageButtons()
	{
		dropPlaceholder.getStyleClass().add("page-button-drop-placeholder");
		dropPlaceholder.setManaged(true);

		installPageButtonsFlowPaneDropBehavior();
	}

	private void installPageButtonsFlowPaneDropBehavior()
	{
		setOnDragOver(e -> {
			if(draggedPageButton == null || e.getDragboard() == null || !e.getDragboard().hasContent(PAGE_BUTTON_DND))
			{
				return;
			}

			e.acceptTransferModes(TransferMode.MOVE);

			int targetIndex = computeInsertIndexForPointer(e.getSceneX(), e.getSceneY());
			movePlaceholderToIndex(targetIndex);

			e.consume();
		});

		setOnDragDropped(e -> {
			if(draggedPageButton == null)
			{
				e.setDropCompleted(false);
				e.consume();
				return;
			}

			// Defensive: falls der Button aus irgendeinem Grund noch im FlowPane ist
			getChildren().remove(draggedPageButton);

			int insertIndex = getChildren().indexOf(dropPlaceholder);
			removePlaceholder();

			// Do never move the page button behind "page-add-button"
			int maxIndex = Math.max(0, getChildren().size() - 1);
			insertIndex = Math.clamp(insertIndex, 0, maxIndex);

			getChildren().add(insertIndex, draggedPageButton);
			draggedPageButton.getStyleClass().remove(PAGE_BUTTON_DRAGGING_STYLECLASS);

			draggedPageButton = null;
			originalDraggedIndex = -1;

			e.setDropCompleted(true);
			e.consume();
		});
	}

	void buildPageButtons(List<Page> pages, BiConsumer<Button, Page> onButtonCreate)
	{
		getChildren().removeIf(node -> node.getUserData() != null);
		for(Page page : pages)
		{
			final Button button = new Button(page.getName());
			button.getStyleClass().add("page-button");
			button.setFocusTraversable(false);
			button.setUserData(page);
			onButtonCreate.accept(button, page);

			button.setOnDragDetected(e -> startPageButtonDrag(e, button));
			button.setOnDragDone(this::onDragDone);

			getChildren().add(getChildren().size() - 1, button);
		}
	}

	private void startPageButtonDrag(MouseEvent e, Button button)
	{
		if(isLoading)
		{
			e.consume();
			return;
		}

		// DnD muss gestartet werden, solange der Node noch in der Scene ist
		final Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
		final ClipboardContent content = new ClipboardContent();
		content.put(PAGE_BUTTON_DND, "page-button");
		db.setContent(content);

		WritableImage img = button.snapshot(new SnapshotParameters(), null);
		db.setDragView(img, img.getWidth() / 2.0, img.getHeight() / 2.0);

		draggedPageButton = button;
		originalDraggedIndex = getChildren().indexOf(button);

		button.getStyleClass().add(PAGE_BUTTON_DRAGGING_STYLECLASS);

		// Placeholder-Größe an den echten Button anlehnen
		double placeholderWidth = Math.max(button.getWidth(), button.prefWidth(-1));
		double placeholderHeight = Math.max(button.getHeight(), button.prefHeight(-1));
		dropPlaceholder.setPrefSize(placeholderWidth, placeholderHeight);
		dropPlaceholder.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		// WICHTIG: Jetzt (nach startDragAndDrop) sofort aus dem Layout nehmen
		if(getChildren().contains(button))
		{
			getChildren().remove(button);
			if(!getChildren().contains(dropPlaceholder))
			{
				getChildren().add(originalDraggedIndex, dropPlaceholder);
			}
		}

		e.consume();
	}

	private void onDragDone(DragEvent dragDoneEvent)
	{
		if(!dragDoneEvent.isDropCompleted() && draggedPageButton != null)
		{
			removePlaceholder();
			if(!getChildren().contains(draggedPageButton))
			{
				getChildren().add(originalDraggedIndex, draggedPageButton);
			}

			draggedPageButton.getStyleClass().remove(PAGE_BUTTON_DRAGGING_STYLECLASS);
			draggedPageButton = null;
			originalDraggedIndex = -1;
		}
		dragDoneEvent.consume();
	}

	private int computeInsertIndexForPointer(double sceneX, double sceneY)
	{
		// Do not allow dragging page button after "page-add-button"
		int maxIndex = Math.max(0, getChildren().size() - 1);

		// Collect all page buttons (except placeholder)
		final List<Bounds> pageBounds = new ArrayList<>();

		for(int i = 0; i < maxIndex; i++)
		{
			final Node n = getChildren().get(i);
			if(n == dropPlaceholder || !isPageButton(n))
			{
				continue;
			}

			final Bounds b = n.localToScene(n.getBoundsInLocal());
			pageBounds.add(b);
		}

		// If no buttons are present, index 0 is correct
		if(pageBounds.isEmpty())
		{
			return 0;
		}

		// Stabiler "vor den ersten Tab" Bereich:
		// - links vom linken Rand des linken Tabs
		// - oder im oberen Tab-Strip (erste Zeile) links vom ersten Tab-Mittelpunkt
		final double leftMostX = pageBounds.stream().mapToDouble(Bounds::getMinX).min().orElse(Double.NaN);

		// Ermittele erste Zeile (FlowPane kann umbrechen)
		final double topRowMinY = pageBounds.stream().mapToDouble(Bounds::getMinY).min().orElse(Double.NaN);
		final double topRowMaxY = pageBounds.stream()
				.filter(b -> Math.abs(b.getMinY() - topRowMinY) < 2.0)
				.mapToDouble(Bounds::getMaxY)
				.max()
				.orElse(topRowMinY);

		final Bounds firstInTopRow = pageBounds.stream()
				.filter(b -> Math.abs(b.getMinY() - topRowMinY) < 2.0)
				.min(Comparator.comparingDouble(Bounds::getMinX))
				.orElse(pageBounds.getFirst());

		double firstTopRowMidX = (firstInTopRow.getMinX() + firstInTopRow.getMaxX()) / 2.0;

		boolean inTopStrip = sceneY <= (topRowMaxY + 8.0); // kleiner Puffer nach unten
		boolean leftOfAll = sceneX < (leftMostX - 8.0);    // kleiner Puffer nach links
		boolean leftOfFirst = sceneX < firstTopRowMidX;

		if(leftOfAll || (inTopStrip && leftOfFirst))
		{
			return 0;
		}

		// Normalfall: nächstgelegenen Tab finden und links/rechts davon einfügen
		int bestIndex = maxIndex; // default: direkt vor Add-Button
		double bestScore = Double.POSITIVE_INFINITY;

		for(int i = 0; i < maxIndex; i++)
		{
			final Node node = getChildren().get(i);
			if(node == dropPlaceholder || !isPageButton(node))
			{
				continue;
			}

			final Bounds b = node.localToScene(node.getBoundsInLocal());
			double midX = (b.getMinX() + b.getMaxX()) / 2.0;

			boolean sameRow = sceneY >= b.getMinY() && sceneY <= b.getMaxY();
			double rowPenalty = sameRow ? 0.0 : 10_000.0;

			double dx = Math.abs(sceneX - midX);
			double dy = Math.abs(sceneY - (b.getMinY() + b.getMaxY()) / 2.0);

			double score = rowPenalty + dx + dy * 0.5;
			if(score < bestScore)
			{
				bestScore = score;
				bestIndex = (sceneX < midX) ? i : (i + 1);
			}
		}

		return Math.min(bestIndex, maxIndex);
	}

	private void movePlaceholderToIndex(int targetIndex)
	{
		int current = getChildren().indexOf(dropPlaceholder);
		if(current == targetIndex)
		{
			return;
		}

		if(current >= 0)
		{
			getChildren().remove(current);
		}

		int addIndex = Math.max(0, getChildren().size() - 1);

		targetIndex = Math.clamp(targetIndex, 0, addIndex);
		getChildren().add(targetIndex, dropPlaceholder);
	}

	private void removePlaceholder()
	{
		getChildren().remove(dropPlaceholder);
	}

	void highlightPageButton(Page page)
	{
		getChildren().forEach(node -> node.getStyleClass().remove(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS));
		getChildren().stream()
				.filter(button -> Objects.equals(button.getUserData(), page))
				.findFirst()
				.ifPresent(button -> button.getStyleClass().add(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS));
	}

	private boolean isPageButton(Object node)
	{
		return (node instanceof Node n) && (n.getUserData() instanceof Page);
	}
}
