package org.macausmp.sportsday.gui;

public interface IPageableGUI extends IPluginGUI {

    int getPage();

    int getMaxPage();

    int getStartSlot();

    int getEndSlot();

    default int getSize() {
        return getEndSlot() - getStartSlot();
    }
}
