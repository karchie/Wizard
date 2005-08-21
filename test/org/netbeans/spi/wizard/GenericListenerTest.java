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
 * GenericListenerTest.java
 * JUnit based test
 *
 * Created on August 20, 2005, 10:51 AM
 */

package org.netbeans.spi.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import junit.framework.*;
import java.util.EventObject;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;




/**
 * Tests that the generic listener for all different component types works.
 *
 * @author Tim Boudreau
 */
public class GenericListenerTest extends TestCase {
    
    public GenericListenerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GenericListenerTest.class);
        return suite;
    }

    private GenericListener gl = null;
    private WP wp = null;
    protected void setUp() throws Exception {
        System.setProperty ("WizardPage.log", "true");
        wp = new WP();
        assertNotNull (gl);
    }
    
    private void assertListenedTo (JPanel pnl) {
        assertTrue (Arrays.asList(pnl.getContainerListeners()).contains(gl));
    }
    
    public void testVisualComponentsWork() throws Exception {
        System.setProperty("WizardPage.listener.log", "true");
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout (new BorderLayout());
        LotsOfComponentsPanel lcp = new LotsOfComponentsPanel();
        jf.getContentPane().add (lcp);
        jf.setBounds(20, 20, 800, 600);
        jf.setVisible(true);
        //Wait for the frame to be shown
        Thread.currentThread().sleep (1000);
        lcp.tickleAll();
    }
/*
    public void testImmediateChildrenListenedTo() {
        System.out.println("testImmediateChildrenListenedTo");

        JCheckBox cb = new JCheckBox();
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        wp.add (cb);
        assertTrue (Arrays.asList(cb.getActionListeners()).contains(gl));
        
        
        JComboBox box = new JComboBox ();
        box.getModel().setSelectedItem("Hooey");
        box.setName("hooeyBox");
        wp.add (box);
        assertTrue (Arrays.asList(box.getActionListeners()).contains(gl));
        
        
        wp.assertPair ("hooeyBox", "Hooey");
        wp.assertPair ("checkBox", Boolean.TRUE);
    }
    
    public void testIndirectChildrenListenedToIfPanelAddedLast() {
        System.out.println("testIndirectChildrenListenedToIfPanelAddedLast");

        JPanel jp = new JPanel();
        
        JCheckBox cb = new JCheckBox();
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        jp.add (cb);
        
        
        JComboBox box = new JComboBox ();
        box.getModel().setSelectedItem("Hooey");
        box.setName("hooeyBox");
        jp.add (box);
        
        wp.add (jp);
        
        assertTrue (Arrays.asList(box.getActionListeners()).contains(gl));
        assertTrue (Arrays.asList(cb.getActionListeners()).contains(gl));
        
        
        wp.assertPair ("hooeyBox", "Hooey");
        wp.assertPair ("checkBox", Boolean.TRUE);
    }    
    
    public void testIndirectChildrenListenedToIfPanelAddedFirst() {
        System.out.println("testIndirectChildrenListenedToIfPanelAddedFirst");

        JPanel jp = new JPanel();
        wp.add (jp);
        
        JCheckBox cb = new JCheckBox();
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        jp.add (cb);
        
        
        JComboBox box = new JComboBox ();
        box.getModel().setSelectedItem("Hooey");
        box.setName("hooeyBox");
        jp.add (box);
        
        assertTrue (Arrays.asList(box.getActionListeners()).contains(gl));
        assertTrue (Arrays.asList(cb.getActionListeners()).contains(gl));
        
        
        wp.assertPair ("hooeyBox", "Hooey");
        wp.assertPair ("checkBox", Boolean.TRUE);
    }    
    
    public void testVeryIndirectChildrenListenedToIfPanelAddedFirst() {
        System.out.println("testIndirectChildrenListenedToIfPanelAddedFirst");

        JPanel pnl = new JPanel();
        JPanel pnl2 = new JPanel();
        pnl.add (pnl2);
        JPanel pnl3 = new JPanel();
        pnl2.add (pnl3);
        JPanel jp = new JPanel();
        pnl3.add (jp);
        wp.add (jp);
        
        JCheckBox cb = new JCheckBox();
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        jp.add (cb);
        
        
        JComboBox box = new JComboBox ();
        box.getModel().setSelectedItem("Hooey");
        box.setName("hooeyBox");
        jp.add (box);
        
        assertTrue (Arrays.asList(box.getActionListeners()).contains(gl));
        assertTrue (Arrays.asList(cb.getActionListeners()).contains(gl));
        
        wp.assertPair ("hooeyBox", "Hooey");
        wp.assertPair ("checkBox", Boolean.TRUE);
    }  
    
    public void testValuesDisappearWhenChildrenRemoved() {
        System.out.println("testValuesDisappearWhenChildrenRemoved");

        JPanel jp = new JPanel();
        wp.add (jp);
        
        JCheckBox cb = new JCheckBox();
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        jp.add (cb);
        
        
        JComboBox box = new JComboBox ();
        box.getModel().setSelectedItem("Hooey");
        box.setName("hooeyBox");
        jp.add (box);
        
        assertTrue (Arrays.asList(box.getActionListeners()).contains(gl));
        assertTrue (Arrays.asList(cb.getActionListeners()).contains(gl));
        
        
        wp.assertPair ("hooeyBox", "Hooey");
        wp.assertPair ("checkBox", Boolean.TRUE);
        
        jp.remove (box);
        
        wp.assertNotPresent("hooeyBox");
        
        assertFalse ("Checkbox removed from panel, but GenericListener is" +
                " still listening to it", 
                Arrays.asList(box.getActionListeners()).contains(gl));
        
    } 
    
    
    public void testValuesDisappearWhenAncestorRemoved() {
        System.out.println("testValuesDisappearWhenAncestorRemoved");

        JPanel pnl = new JPanel();
        JPanel pnl2 = new JPanel();
        pnl.add (pnl2);
        JPanel pnl3 = new JPanel();
        pnl2.add (pnl3);
        JPanel jp = new JPanel();
        pnl3.add (jp);

        wp.add (pnl);
        
        assertListenedTo(pnl);
        assertListenedTo(wp);
        assertListenedTo(pnl2);
        assertListenedTo(pnl3);
        assertListenedTo(jp);
        
        
        JCheckBox cb = new JCheckBox();
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        jp.add (cb);
        
        
        JComboBox box = new JComboBox ();
        box.getModel().setSelectedItem("Hooey");
        box.setName("hooeyBox");
        jp.add (box);
        
        assertTrue (Arrays.asList(box.getActionListeners()).contains(gl));
        assertTrue (Arrays.asList(cb.getActionListeners()).contains(gl));
        
        
        wp.assertPair ("hooeyBox", "Hooey");
        wp.assertPair ("checkBox", Boolean.TRUE);

        cb.doClick();
        wp.assertPair ("checkBox", Boolean.FALSE);
        
        pnl.remove (pnl2);
        
        wp.assertNotPresent("hooeyBox");
        wp.assertNotPresent("checkBox");
        
        assertFalse ("Checkbox removed from panel, but GenericListener is" +
                " still listening to it", 
                Arrays.asList(box.getActionListeners()).contains(gl));
        
        assertFalse ("Checkbox removed from panel, but GenericListener is" +
                " still listening to it", 
                Arrays.asList(cb.getActionListeners()).contains(gl));
        
    }     
    
    public void testPlainPanelUseWorks() {
        JTextField fld = new JTextField();
        wp.add (fld);
        
        //XXX send some keystrokes, check validation
        
    }

    
    public void testRenamingComponentChangesMapKey() {
        System.out.println("testRenamingFieldChangesMapKey");
        //XXX maybe delete all this and don't support name changes?
        
        //Most components don't support change events for setName(), but we
        //want to support ones that potentially do
        JCheckBox cb = new JCheckBox() {
            public void setName(String name) {
                String old = getName();
                super.setName(name);
                if (old != null) {
                    firePropertyChange("name", old, name);
                }
            }
        };
        cb.getModel().setSelected(true);
        cb.setName ("checkBox");
        wp.add (cb);
        
        wp.assertPair ("checkBox", Boolean.TRUE);
        
        final boolean[] fired = new boolean[] { false };
        cb.addPropertyChangeListener (new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                System.err.println(" ***** PROPERTY CHANGE! " + 
                        pce.getPropertyName() + " " + pce.getOldValue() +
                        "->" + pce.getNewValue());
                if ("name".equals(pce.getPropertyName())) {
                    fired[0] = true;
                }
            }
        });
        
        cb.setName ("nue");
        try { Thread.currentThread().sleep (500); } catch (Exception e) {}
        if (fired[0]) {
            wp.assertPair("nue", Boolean.TRUE);
            wp.assertNotPresent("checkBox");
        }
    }
 */
    
    
    private class WP extends WizardPage {
        public WP () {
            super ("step", "this is a step", false);
            gl = new GenericListener(this);
        }
        
        public Object get(Object key) {
            return getWizardData(key);
        }

        private Object evt = null;
        protected String validateContents(Component component, Object event) {
            evt = event;
            return null;
        }
        
        public void assertValidated() {
            assertValidated (null);
        }
        
        public void assertValidated(String msg) {
            Object old = evt;
            evt = null;
            assertNotNull (old);
        }
        
        public void assertNotValidated() {
            assertNull (evt);
        }
        
        public void assertPair (Object key, Object val) {
            assertEquals ("Didn't find or wrong value for " + key + " in " + 
                    getWizardDataMap() + "; expected " + val + 
                    " got " + get(key), val, get(key));
        }
        
        public void assertNotPresent (Object key) {
            assertNull (get(key));
        }
        
        public void assertEventSource (Object o) {
            Object old = evt;
            assertValidated();
            assertTrue ("Wrong event type: " + old, old instanceof EventObject);
            assertSame (o, ((EventObject) old).getSource());
        }
    }
    
    
/*
    public void testAttachTo() {
        Component jc = null;
        GenericListener instance = null;
        
        instance.attachTo(jc);
    }

    public void testDetachFrom() {
        Component jc = null;
        GenericListener instance = null;
        
        instance.detachFrom(jc);
    }

    public void testAccept() {
        Component jc = null;
        
        boolean expResult = true;
        boolean result = GenericListener.accept(jc);
        assertEquals(expResult, result);
    }

    public void testSetIgnoreEvents() {
        boolean val = true;
        GenericListener instance = null;
        
        instance.setIgnoreEvents(val);
    }

    public void testInsertUpdate() {
        DocumentEvent e = null;
        GenericListener instance = null;
        
        instance.insertUpdate(e);
    }

    public void testChangedUpdate() {
        DocumentEvent e = null;
        GenericListener instance = null;
        
        instance.changedUpdate(e);
    }

    public void testRemoveUpdate() {
        DocumentEvent e = null;
        GenericListener instance = null;
        
        instance.removeUpdate(e);
    }

    public void testStateChanged() {
        ChangeEvent e = null;
        GenericListener instance = null;
        
        instance.stateChanged(e);
    }

    public void testActionPerformed() {
        ActionEvent e = null;
        GenericListener instance = null;
        
        instance.actionPerformed(e);
    }

    public void testValueChanged() {
        ListSelectionEvent e = null;
        GenericListener instance = null;
        
        instance.valueChanged(e);
    }

    public void testTableChanged() {
        TableModelEvent e = null;
        GenericListener instance = null;
        
        instance.tableChanged(e);
    }

    public void testHierarchyChanged() {
        HierarchyEvent e = null;
        GenericListener instance = null;
        
        instance.hierarchyChanged(e);
    }

    public void testPropertyChange() {
        PropertyChangeEvent evt = null;
        GenericListener instance = null;
        
        instance.propertyChange(evt);
    }
    */
    
}
