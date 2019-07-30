package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.util.stream.Collectors;

import org.vaadin.stefan.dnd.drop.DropTargetExtension;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * List of components placed on a page that renders them and allow drag/drop functionality
 */
public class PageContentsArea extends Div implements PageContentActionHandler {

	private static final long serialVersionUID = -5743090879640188079L;

	private VerticalLayout placeholderComponent;

	public PageContentsArea() {
		DropTargetExtension<PageContentsArea> droppable = DropTargetExtension.extend(this);

		droppable.addDragEnterListener(e -> {
			addClassName("is-dropping");
		});

		droppable.addDragOverListener(e -> {
			addClassName("is-dropping");
		});

		droppable.addDragLeaveListener(e -> {
			removeClassName("is-dropping");
		});

		droppable.addDropListener(event -> {
			event.getDragSource().ifPresent(e -> {
				if (e.getComponent() instanceof PageContentCard) {
					PageContentCard contentCard = (PageContentCard) e.getComponent();

					if (placeholderComponent.getParent().isPresent()) {
						removeAll();
					}

					add(new PageContent(PageContentsArea.this, contentCard.getComponent()));
				}
			});

			removeClassName("is-dropping");
		});

		placeholderComponent = new PageContentsAreaPlaceholder();

		add(placeholderComponent);
		addClassName("page-contents-area");
	}

	public void clearContents() {
		removeAll();
		add(placeholderComponent);
	}

	@Override
	public void moveUp(PageContent content) {
		int index = getChildren().collect(Collectors.toList()).indexOf(content);
		if (index > 0) {
			remove(content);
			addComponentAtIndex(index - 1, content);
		}
	}

	@Override
	public void moveDown(PageContent content) {
		int index = getChildren().collect(Collectors.toList()).indexOf(content);
		if (index < (getChildren().count() - 1)) {
			remove(content);
			addComponentAtIndex(index + 1, content);
		}
	}

	@Override
	public void remove(PageContent content) {
		super.remove(content);

		if (!getChildren().findFirst().isPresent()) {
			add(placeholderComponent);
		}
	}

}
