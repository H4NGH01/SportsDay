package org.macausmp.sportsday.gui;

public interface Pageable {
    /**
     * Gets current page number.
     * @return number of current page
     */
    int getPage();

    /**
     * Gets page count of gui.
     * @return page count of gui
     */
    int getMaxPage();

    /**
     * Gets the slot where the content item starts.
     * @return slot where the content item starts
     */
    int getStartSlot();

    /**
     * Gets the slot where the content item ends.
     * @return slot where the content item ends
     */
    int getEndSlot();

    /**
     * Gets the size of content item.
     * @return size of content item
     */
    default int getSize() {
        return getEndSlot() - getStartSlot();
    }
}
