/*
 * Copyright (C) 2011-2014 Aiden Lab - All Rights Reserved
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Aiden Lab All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. Aiden Lab is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

package juicebox.windowui;

import org.broad.igv.Globals;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Ido Machol, Muhammad S Shamim, Neva Durand
 */
public abstract class RecentMenu extends JMenu {
    private static final long serialVersionUID = 4685393080959162312L;
    private final int m_maxItems;
    private final String m_entry;
    private final Preferences prefs = Preferences.userNodeForPackage(Globals.class);
    private List<String> m_items = new ArrayList<String>();

    public RecentMenu(String name, int count, String prefEntry) {
        super(name);

        this.m_maxItems = count;
        this.m_entry = prefEntry;
        String[] recentEntries = new String[count];
        Arrays.fill(recentEntries, "");

        boolean addedItem = false;
        // load recent positions from properties
        for (int i = this.m_maxItems-1; i >= 0; i--) {
            String val = prefs.get(this.m_entry + i, "");
            if (!val.equals("")) {
                addEntry(val, false);
                addedItem = true;
            }
        }
        if (!addedItem) {
            this.setEnabled(false);
        }
    }

    /**
     * Add "Clear" menu item to bottom of this list
     */
    private void addClearItem() {
        //---- Clear Recent ----
        JMenuItem clearMapList = new JMenuItem();
        clearMapList.setText("Clear ");
        clearMapList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Clear all items from preferences:
                for (int i = 0; i < m_maxItems; i++) {
                    prefs.remove(m_entry + i);
                }
                //clear the existing items
                removeAll();
                m_items = new ArrayList<String>();
                setEnabled(false);
            }
        });
        addSeparator();
        add(clearMapList);
    }


    /**
     * Add new recent entry, update file and menu
     *
     * @param savedEntry Name and Value of entry.
     * @param updateFile also save to file, Constructor call with false - no need to re-write.
     */
    public void addEntry(String savedEntry, boolean updateFile) {

        //clear the existing items
        this.removeAll();

        //Add item, remove previous existing duplicate:
        m_items.remove(savedEntry);
        m_items.add(0, savedEntry);

        //Chop last item if list is over size:
        if (this.m_items.size() > this.m_maxItems) {
            this.m_items.remove(this.m_items.size() - 1);
        }

        //add items back to the menu
        for (String m_item : this.m_items) {
            String delimiter = "@@";
            String[] temp;
            temp = m_item.split(delimiter);

            if (!temp[0].equals("")) {
                JMenuItem menuItem = new JMenuItem(temp[0]);
                menuItem.setVisible(true);
                menuItem.setToolTipText(temp[0]);
                menuItem.setActionCommand(m_item);
                //menuItem.setActionMap();
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        onSelectPosition(actionEvent.getActionCommand());
                    }
                });
                //menuItem.addMouseListener(new MouseListener() );
                this.add(menuItem);
            }

        }
        //update the file
        if (updateFile) {
            try {
                for (int i = 0; i < this.m_maxItems; i++) {
                    if (i < this.m_items.size()) {
                        prefs.put(this.m_entry + i, this.m_items.get(i));
                    } else {
                        prefs.remove(this.m_entry + i);
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        addClearItem();

        //check if this is disabled
        if (!this.isEnabled()) {
            this.setEnabled(true);
        }
    }

    /**
     * Abstract event, fires when recent map is selected.
     *
     * @param mapPath The file that was selected.
     */
    public abstract void onSelectPosition(String mapPath);


}