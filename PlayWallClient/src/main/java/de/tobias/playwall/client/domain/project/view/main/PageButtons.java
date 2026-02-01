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

	private Button draggedPageButton;
	private int originalDraggedIndex = -1;

	private final Region pageButtonDropPlaceholder = new Region();

	@Getter
	@Setter
	private boolean isLoading = false;

	public PageButtons()
	{
		pageButtonDropPlaceholder.getStyleClass().add("page-button-drop-placeholder");
		pageButtonDropPlaceholder.setManaged(true);

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

			int insertIndex = getChildren().indexOf(pageButtonDropPlaceholder);
			removePlaceholderIfPresent();

			// Clamp: nie hinter den Add-Button
			int maxIndex = Math.max(0, getChildren().size() - 1);
			insertIndex = Math.min(Math.max(insertIndex, 0), maxIndex);

			getChildren().add(insertIndex, draggedPageButton);
			draggedPageButton.getStyleClass().remove("page-button-dragging");

			draggedPageButton = null;
			originalDraggedIndex = -1;

			e.setDropCompleted(true);
			e.consume();
		});

		// DragDone bleibt am Source-Button (wie du es schon hast)
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

			installPageButtonDragAndDrop(button);

			getChildren().add(getChildren().size() - 1, button);
		}
	}

	private void installPageButtonDragAndDrop(Button button)
	{
		button.setOnDragDetected(e -> startPageButtonDrag(e, button));
	}

	private void startPageButtonDrag(MouseEvent e, Button button)
	{
		if(isLoading)
		{
			e.consume();
			return;
		}

		// DnD muss gestartet werden, solange der Node noch in der Scene ist
		Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
		ClipboardContent content = new ClipboardContent();
		content.put(PAGE_BUTTON_DND, "page-button");
		db.setContent(content);

		WritableImage img = button.snapshot(new SnapshotParameters(), null);
		db.setDragView(img, img.getWidth() / 2.0, img.getHeight() / 2.0);

		draggedPageButton = button;
		originalDraggedIndex = getChildren().indexOf(button);

		button.getStyleClass().add("page-button-dragging");

		// Placeholder-Größe an den echten Button anlehnen
		double w = Math.max(button.getWidth(), button.prefWidth(-1));
		double h = Math.max(button.getHeight(), button.prefHeight(-1));
		pageButtonDropPlaceholder.setPrefSize(w, h);
		pageButtonDropPlaceholder.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

		// WICHTIG: Jetzt (nach startDragAndDrop) sofort aus dem Layout nehmen
		if(getChildren().contains(button))
		{
			getChildren().remove(button);

			int maxIndex = Math.max(0, getChildren().size() - 1); // vor Add-Button
			int placeholderIndex = Math.min(Math.max(originalDraggedIndex, 0), maxIndex);

			if(!getChildren().contains(pageButtonDropPlaceholder))
			{
				getChildren().add(placeholderIndex, pageButtonDropPlaceholder);
			}
		}

		// Restore/Finalize MUSS am Source hängen, sonst verschwindet der Button beim Drop außerhalb
		button.setOnDragDone(dragDoneEvent -> {
			if(!dragDoneEvent.isDropCompleted() && draggedPageButton != null)
			{
				removePlaceholderIfPresent();

				int maxIndex = Math.max(0, getChildren().size() - 1);
				int restoreIndex = Math.min(Math.max(originalDraggedIndex, 0), maxIndex);

				if(!getChildren().contains(draggedPageButton))
				{
					getChildren().add(restoreIndex, draggedPageButton);
				}

				draggedPageButton.getStyleClass().remove("page-button-dragging");
				draggedPageButton = null;
				originalDraggedIndex = -1;
			}

			dragDoneEvent.consume();
		});

		e.consume();
	}

	private int computeInsertIndexForPointer(double sceneX, double sceneY)
	{
		// Wir erlauben nur Indizes vor dem Add-Button
		int addIndex = Math.max(0, getChildren().size() - 1);

		// Sammle alle "echten" Page-Buttons (ohne Placeholder) inkl. Scene-Bounds
		final List<Node> pageNodes = new ArrayList<>();
		final List<Bounds> pageBounds = new ArrayList<>();

		for(int i = 0; i < addIndex; i++)
		{
			Node n = getChildren().get(i);
			if(n == pageButtonDropPlaceholder)
			{
				continue;
			}
			if(!isPageButton(n))
			{
				continue;
			}

			Bounds b = n.localToScene(n.getBoundsInLocal());
			pageNodes.add(n);
			pageBounds.add(b);
		}

		// Wenn es keine Tabs gibt, ist Index 0 korrekt
		if(pageNodes.isEmpty())
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

		Bounds firstInTopRow = pageBounds.stream()
				.filter(b -> Math.abs(b.getMinY() - topRowMinY) < 2.0)
				.min(Comparator.comparingDouble(Bounds::getMinX))
				.orElse(pageBounds.get(0));

		double firstTopRowMidX = (firstInTopRow.getMinX() + firstInTopRow.getMaxX()) / 2.0;

		boolean inTopStrip = sceneY <= (topRowMaxY + 8.0); // kleiner Puffer nach unten
		boolean leftOfAll = sceneX < (leftMostX - 8.0);    // kleiner Puffer nach links
		boolean leftOfFirst = sceneX < firstTopRowMidX;

		if(leftOfAll || (inTopStrip && leftOfFirst))
		{
			return 0;
		}

		// Normalfall: nächstgelegenen Tab finden und links/rechts davon einfügen
		int bestIndex = addIndex; // default: direkt vor Add-Button
		double bestScore = Double.POSITIVE_INFINITY;

		for(int i = 0; i < addIndex; i++)
		{
			Node n = getChildren().get(i);
			if(n == pageButtonDropPlaceholder)
			{
				continue;
			}
			if(!isPageButton(n))
			{
				continue;
			}

			Bounds b = n.localToScene(n.getBoundsInLocal());
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

		return Math.min(bestIndex, addIndex);
	}

	private void movePlaceholderToIndex(int targetIndex)
	{
		int current = getChildren().indexOf(pageButtonDropPlaceholder);
		if(current == targetIndex)
		{
			return;
		}

		if(current >= 0)
		{
			getChildren().remove(current);
		}

		int addIndex = Math.max(0, getChildren().size() - 1);

		targetIndex = Math.min(Math.max(targetIndex, 0), addIndex);
		getChildren().add(targetIndex, pageButtonDropPlaceholder);
	}

	private void removePlaceholderIfPresent()
	{
		getChildren().remove(pageButtonDropPlaceholder);
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
