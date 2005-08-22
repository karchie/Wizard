/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * GenericListener.java
 *
 * Created on October 5, 2004, 12:36 AM
 */

package org.netbeans.spi.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeSelectionModel;


/**
 * A listener that can listen to just about any standard swing component that
 * accepts user input, and notify the panel that it needs to validate its
 * contents
 *
 * @author Tim Boudreau
 */
final class GenericListener implements ActionListener, PropertyChangeListener, ContainerListener,DocumentListener, ChangeListener, ListSelectionListener, TreeSelectionListener, TableModelListener {
    private final WizardPage pnl;
    private static final boolean LOG = Boolean.getBoolean (
            "WizardPage.listener.log"); //NOI18N
    GenericListener (WizardPage pnl) {
        this.pnl = pnl;
        pnl.addContainerListener(this);
    }

    /** Set of components that we're listening to models of, so we can look
     * up the component from the model as needed */
    private Set listenedTo = new HashSet();
    
    void attachTo (Component jc) {
        //XXX do mapping model -> component?
        if (jc instanceof JPanel || jc instanceof JScrollPane || jc instanceof JViewport) {
            attachToHierarchyOf ((Container) jc);
        } else if (jc instanceof JList) {
            listenedTo.add (jc);
            ((JList) jc).addListSelectionListener(this);
        } else if (jc instanceof JComboBox) {
            ((JComboBox) jc).addActionListener(this);
        } else if (jc instanceof JTree) {
            listenedTo.add (jc);
            ((JTree) jc).getSelectionModel().addTreeSelectionListener(this);
        } else if (jc instanceof JToggleButton) {
            ((AbstractButton) jc).addActionListener(this);
        } else if (jc instanceof JTextComponent) {
            listenedTo.add (jc);
            ((JTextComponent) jc).getDocument().addDocumentListener(this);
        } else if (jc instanceof JColorChooser) {
            listenedTo.add (jc);
            ((JColorChooser) jc).getSelectionModel().addChangeListener(this);
        } else if (jc instanceof JSpinner) {
            ((JSpinner) jc).addChangeListener(this);
        } else if (jc instanceof JSlider) {
            ((JSlider) jc).addChangeListener(this);
        } else if (jc instanceof JTable) {
            listenedTo.add (jc);
            ((JTable) jc).getSelectionModel().addListSelectionListener(this);
        } else {
            //XXX
            if (LOG) log("Don't know how to listen to a " + //NOI18N
                    jc.getClass().getName()); 
        }
        if (accept (jc) && !(jc instanceof JPanel)) {
            jc.addPropertyChangeListener("name", this); //NOI18N
            if (pnl.getMapKeyFor(jc) != null) {
                pnl.maybeUpdateMap((JComponent) jc);
            }
        }
        if (LOG && accept(jc)) {
            log ("Begin listening to " + jc);
        }
    }
    
    void detachFrom (Component jc) {
        listenedTo.remove(jc);
        if (jc instanceof JPanel || jc instanceof JScrollPane || jc instanceof JViewport) {
            detachFromHierarchyOf ((Container) jc);
        } else if (jc instanceof JList) {
            ((JList) jc).removeListSelectionListener(this);
        } else if (jc instanceof JComboBox) {
            ((JComboBox) jc).removeActionListener(this);
        } else if (jc instanceof JTree) {
            ((JTree) jc).getSelectionModel().removeTreeSelectionListener(this);
        } else if (jc instanceof JToggleButton) {
            ((AbstractButton) jc).removeActionListener(this);
        } else if (jc instanceof JTextComponent) {
            ((JTextComponent) jc).getDocument().removeDocumentListener(this);
        } else if (jc instanceof JColorChooser) {
            ((JColorChooser) jc).getSelectionModel().removeChangeListener(this);
        } else if (jc instanceof JSpinner) {
            ((JSpinner) jc).removeChangeListener(this);
        } else if (jc instanceof JSlider) {
            ((JSlider) jc).removeChangeListener(this);
        } else if (jc instanceof JTable) {
            ((JTable) jc).getSelectionModel().removeListSelectionListener(this);
        }
        if (accept (jc) && !(jc instanceof JPanel)) {
            jc.removePropertyChangeListener("name", this); //NOI18N
            Object key = pnl.getMapKeyFor(jc);
            if (key != null) {
                if (LOG) log ("Named component removed from hierarchy: " + //NOI18N
                        key + ".  Removing any corresponding " + //NOI18N
                        "value from the wizard settings map."); //NOI18N
                pnl.removeFromMap(key);
            }
        }
        if (LOG && accept(jc)) {
            log ("Stop listening to " + jc); //NOI18N
        }
    }
    
    private void detachFromHierarchyOf(Container pnl) {
        pnl.removeContainerListener(this);
        Component[] c = pnl.getComponents();
        for (int i=0; i < c.length; i++) {
            detachFrom (c[i]); //Will callback recursively any nested JPanels
        }
    }    
    
    private void attachToHierarchyOf(Container pnl) {
        pnl.addContainerListener(this);
        Component[] c = pnl.getComponents();
        for (int i=0; i < c.length; i++) {
            attachTo (c[i]); //Will recursively add any child components in
                             //child panels
        }
    }
   
    static boolean accept (Component jc) {
        if (!(jc instanceof JComponent)) {
            return false;
        }
        return jc instanceof JList ||
               jc instanceof JComboBox ||
               jc instanceof JTree ||
               jc instanceof JToggleButton || //covers toggle, radio, checkbox
               jc instanceof JTextComponent ||
               jc instanceof JColorChooser ||
               jc instanceof JSpinner ||
               jc instanceof JSlider ||
               jc instanceof JPanel ||
               jc instanceof JScrollPane ||
               jc instanceof JViewport;
    }
    
    private boolean ignoreEvents = false;
    void setIgnoreEvents (boolean val) {
        ignoreEvents = val;
    }
    
    private void fire(Object e) {
        if (!ignoreEvents) {
            setIgnoreEvents(true);
            try {
                //XXX this could be prettier...
                if (LOG) log ("Event received: " + e); //NOI18N
                if (e instanceof EventObject && ((EventObject) e).getSource() instanceof Component) { 
                    pnl.userInputReceived((Component)((EventObject) e).getSource(), e);
                } else if (e instanceof TreeSelectionEvent) {
                    if (LOG) log ("Looking for a tree for a tree selection event");
                    TreeSelectionModel mdl = (TreeSelectionModel) ((TreeSelectionEvent)e).getSource();
                    for (Iterator i=listenedTo.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof JTree && ((JTree) o).getSelectionModel() == mdl) {
                            if (LOG) log ("  found it: " + o);
                            pnl.userInputReceived ((Component)o, e);
                            return;
                        }
                    }
                } else if (e instanceof DocumentEvent) {
                    if (LOG) log ("Looking for a JTextComponent for a DocumentEvent");
                    Document d = ((DocumentEvent) e).getDocument();
                    for (Iterator i=listenedTo.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof JTextComponent && ((JTextComponent) o).getDocument() == d) {
                            if (LOG) log ("  found it: " + o);
                            pnl.userInputReceived((Component)o, e);
                            return;
                        }
                    }
                } else if (e instanceof ListSelectionEvent) {
                    if (LOG) log ("Looking for a JList or JTable for a ListSelectionEvent ");
                    ListSelectionModel mdl = (ListSelectionModel) ((ListSelectionEvent) e).getSource();
                    for (Iterator i=listenedTo.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof JList && ((JList) o).getSelectionModel() == mdl) {
                            if (LOG) log ("  found it: " + o);
                            pnl.userInputReceived((Component)o, e);
                            return;
                        } else if (o instanceof JTable && ((JTable) o).getSelectionModel() == mdl) {
                            if (LOG) log ("  found it: " + o);
                            pnl.userInputReceived((Component)o, e);
                            return;
                        }
                    }
                } else {
                    pnl.userInputReceived(null, e);
                }
            } finally {
                setIgnoreEvents(false);
            }
        }
    }
    
    
    
    
    public void insertUpdate (DocumentEvent e) {
        fire(e);
    }
    
    public void changedUpdate (DocumentEvent e) {
        fire(e);
    }
    
    public void removeUpdate (DocumentEvent e) {
        fire(e);
    }
    
    public void stateChanged (ChangeEvent e) {
        fire(e);
    }
    
    public void actionPerformed (ActionEvent e) {
        fire(e);
    }
    
    public void valueChanged (ListSelectionEvent e) {
        fire(e);
    }
    
    public void valueChanged (TreeSelectionEvent e) {
        fire(e);
    }

    public void tableChanged(TableModelEvent e) {
        fire(e);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof JComponent && "name".equals(evt.getPropertyName())) { //NOI18N
            //Note - most components do NOT fire a property change on
            //setName(), but it is possible for this to be done intentionally
            if (evt.getOldValue() instanceof String) {
                if (LOG) {
                    log ("Name of component changed from " + evt.getOldValue() 
                    + " to " + evt.getNewValue() + ". " + " removing any values" +
                            " for " + evt.getOldValue() + " from the wizard " +
                            "data map");
                }
                pnl.removeFromMap((String) evt.getOldValue());
            }
            if (LOG) log ("Possibly update map for renamed component " + 
                    evt.getSource());
            pnl.maybeUpdateMap((JComponent) evt.getSource());
        }
    }

    public void componentAdded(ContainerEvent e) {
        if (accept(e.getChild())) {
            attachTo(e.getChild());
        }
    }

    public void componentRemoved(ContainerEvent e) {
        if (accept(e.getChild())) {
            detachFrom(e.getChild());
        }
    }
    
    private static final void log (String s) {
        System.out.println(s);
    }
}
