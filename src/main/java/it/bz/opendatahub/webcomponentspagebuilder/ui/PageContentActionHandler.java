package it.bz.opendatahub.webcomponentspagebuilder.ui;

/**
 * Handle the various actions from single page contents
 */
public interface PageContentActionHandler {

	void moveUp(PageContent content);

	void moveDown(PageContent content);

	void remove(PageContent content);

}
