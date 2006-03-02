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
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
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
 * A listener that can listen to just about any standard swing component
 * that accepts user input and notify the panel that it needs to
 * validate its contents.
 *
 * @author Tim Boudreau
 */
final class GenericListener
        implements ActionListener, PropertyChangeListener, ItemListener,
        ContainerListener, DocumentListener, ChangeListener,
        ListSelectionListener, TreeSelectionListener, TableModelListener {

    private static final Logger logger =
            Logger.getLogger(GenericListener.class.getName());

    private final WizardPage wizardPage;

    private boolean ignoreEvents;

    /**
     * Set of components that we're listening to models of, so we can look
     * up the component from the model as needed
     */
    private Set listenedTo = new HashSet();

    GenericListener(WizardPage wizardPage) {
        assert wizardPage != null : "WizardPage may not be null"; // NOI18N

        this.wizardPage = wizardPage;
        wizardPage.addContainerListener(this);
    }

    void attachTo(Component jc) {
        //XXX do mapping model -> component?
        if (jc instanceof JPanel || jc instanceof JScrollPane || jc instanceof JViewport) {
            attachToHierarchyOf((Container) jc);
        } else if (jc instanceof JList) {
            listenedTo.add(jc);
            ((JList) jc).addListSelectionListener(this);
        } else if (jc instanceof JComboBox) {
            ((JComboBox) jc).addActionListener(this);
        } else if (jc instanceof JTree) {
            listenedTo.add(jc);
            ((JTree) jc).getSelectionModel().addTreeSelectionListener(this);
        } else if (jc instanceof JToggleButton) {
            ((AbstractButton) jc).addItemListener(this);
        } else if (jc instanceof JTextComponent) {
            listenedTo.add(jc);
            ((JTextComponent) jc).getDocument().addDocumentListener(this);
        } else if (jc instanceof JColorChooser) {
            listenedTo.add(jc);
            ((JColorChooser) jc).getSelectionModel().addChangeListener(this);
        } else if (jc instanceof JSpinner) {
            ((JSpinner) jc).addChangeListener(this);
        } else if (jc instanceof JSlider) {
            ((JSlider) jc).addChangeListener(this);
        } else if (jc instanceof JTable) {
            listenedTo.add(jc);
            ((JTable) jc).getSelectionModel().addListSelectionListener(this);
        } else {
            //XXX
            logger.warning("Don't know how to listen to a " + // NOI18N
                    jc.getClass().getName());
        }

        if (accept(jc) && !(jc instanceof JPanel)) {
            jc.addPropertyChangeListener("name", this);
            if (wizardPage.getMapKeyFor(jc) != null) {
                wizardPage.maybeUpdateMap(jc);
            }
        }

        if (logger.isLoggable(Level.FINE) && accept(jc)) {
            logger.fine("Begin listening to " + jc); // NOI18N
        }
    }

    void detachFrom(Component jc) {
        listenedTo.remove(jc);

        if (jc instanceof JPanel || jc instanceof JScrollPane || jc instanceof JViewport) {
            detachFromHierarchyOf((Container) jc);
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

        if (accept(jc) && !(jc instanceof JPanel)) {
            jc.removePropertyChangeListener("name", this);
            Object key = wizardPage.getMapKeyFor(jc);

            if (key != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Named component removed from hierarchy: " + // NOI18N
                            key + ".  Removing any corresponding " + // NOI18N
                            "value from the wizard settings map."); // NOI18N
                }

                wizardPage.removeFromMap(key);
            }
        }

        if (logger.isLoggable(Level.FINE) && accept(jc)) {
            logger.fine("Stop listening to " + jc); // NOI18N
        }
    }

    private void detachFromHierarchyOf(Container container) {
        container.removeContainerListener(this);
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            detachFrom(components[i]); // Will callback recursively any nested JPanels
        }
    }

    private void attachToHierarchyOf(Container container) {
        container.addContainerListener(this);
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            attachTo(components[i]); // Will recursively add any child components in
        }
    }

    static boolean accept(Component jc) {
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

    void setIgnoreEvents(boolean val) {
        ignoreEvents = val;
    }

    private void fire(Object e) {
        if (!ignoreEvents) {
            setIgnoreEvents(true);
            try {
                //XXX this could be prettier...
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Event received: " + e); // NOI18N
                }
                if (e instanceof EventObject && ((EventObject) e).getSource() instanceof Component) {
                    wizardPage.userInputReceived((Component) ((EventObject) e).getSource(), e);
                } else if (e instanceof TreeSelectionEvent) {
                    logger.fine("Looking for a tree for a tree selection event"); // NOI18N
                    TreeSelectionModel mdl = (TreeSelectionModel) ((TreeSelectionEvent) e).getSource();
                    for (Iterator i = listenedTo.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof JTree && ((JTree) o).getSelectionModel() == mdl) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("  found it: " + o); // NOI18N
                            }
                            wizardPage.userInputReceived((Component) o, e);
                            break;
                        }
                    }
                } else if (e instanceof DocumentEvent) {
                    logger.fine("Looking for a JTextComponent for a DocumentEvent"); // NOI18N
                    Document document = ((DocumentEvent) e).getDocument();
                    for (Iterator i = listenedTo.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof JTextComponent && ((JTextComponent) o).getDocument() == document) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("  found it: " + o); // NOI18N
                            }
                            wizardPage.userInputReceived((Component) o, e);
                            break;
                        }
                    }
                } else if (e instanceof ListSelectionEvent) {
                    logger.fine("Looking for a JList or JTable for a ListSelectionEvent"); // NOI18N
                    ListSelectionModel model = (ListSelectionModel) ((ListSelectionEvent) e).getSource();
                    for (Iterator i = listenedTo.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (o instanceof JList && ((JList) o).getSelectionModel() == model) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("  found it: " + o); // NOI18N
                            }
                            wizardPage.userInputReceived((Component) o, e);
                            break;
                        } else if (o instanceof JTable && ((JTable) o).getSelectionModel() == model) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("  found it: " + o); // NOI18N
                            }
                            wizardPage.userInputReceived((Component) o, e);
                            break;
                        }
                    }
                } else {
                    wizardPage.userInputReceived(null, e);
                }
            } finally {
                setIgnoreEvents(false);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        fire(e);
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() instanceof JComponent && "name".equals(e.getPropertyName())) {
            // Note - most components do NOT fire a property change on
            // setName(), but it is possible for this to be done intentionally
            if (e.getOldValue() instanceof String) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Name of component changed from " + e.getOldValue() + // NOI18N
                            " to " + e.getNewValue() + ".  Removing any values for " +  // NOI18N
                            e.getOldValue() + " from the wizard data map"); // NOI18N
                }
                wizardPage.removeFromMap(e.getOldValue());
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Possibly update map for renamed component " + // NOI18N
                    e.getSource());
            }

            wizardPage.maybeUpdateMap((JComponent) e.getSource());
        }
    }

    public void itemStateChanged(ItemEvent e) {
        fire(e);
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

    public void insertUpdate(DocumentEvent e) {
        fire(e);
    }

    public void changedUpdate(DocumentEvent e) {
        fire(e);
    }

    public void removeUpdate(DocumentEvent e) {
        fire(e);
    }

    public void stateChanged(ChangeEvent e) {
        fire(e);
    }

    public void valueChanged(ListSelectionEvent e) {
        fire(e);
    }

    public void valueChanged(TreeSelectionEvent e) {
        fire(e);
    }

    public void tableChanged(TableModelEvent e) {
        fire(e);
    }
}
