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
 * TrivialWizardFactoryTest.java
 * JUnit based test
 *
 * Created on March 4, 2005, 4:33 PM
 */

package org.netbeans.api.wizard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import junit.framework.*;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.spi.wizard.WizardPanelProvider;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;


/**
 * Tests the default UI for wizards, and, implicitly, a lot of the logic 
 * in the support classes.
 *
 * @author Tim Boudreau
 */
public class DefaultWizardDisplayerTest extends TestCase {
    
    public DefaultWizardDisplayerTest(String testName) {
        super(testName);
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultWizardDisplayerTest.class);
        
        return suite;
    }
    
    protected void setUp() throws Exception {
        System.setProperty ("TrivialWizardFactory.test", "true");
    }
    
    public static JButton[] getButtons() {
        return DefaultWizardDisplayer.buttons;
    }
    
    // A help action for testing
    private static class HA extends AbstractAction {
        ActionEvent ae = null;
        public void actionPerformed (ActionEvent ae) {
            this.ae = ae;
        }
        
        public void assertPerformed() {
            ActionEvent x = ae;
            ae = null;
            assertNotNull (x);
        }
    };
    
    public void testShow() throws Exception {
        System.out.println("testShow");
        PanelProviderImpl impl = new PanelProviderImpl();
        
        Wizard wiz = impl.createWizard();
        
        HA ha = new HA();
        
        show (wiz, ha);
        
        while (!impl.active) {
            Thread.currentThread().sleep (200);
        }
        
        JButton next = DefaultWizardDisplayer.buttons[0];
        JButton prev = DefaultWizardDisplayer.buttons[1];
        JButton finish = DefaultWizardDisplayer.buttons[2];
        JButton cancel = DefaultWizardDisplayer.buttons[3];
        JButton help = DefaultWizardDisplayer.buttons[4];
        
        assertTrue ("Help button should be shown", help.isShowing());
        
        click (help);
        ha.assertPerformed();
        
        assertFalse (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        
        click (impl.cb);
        JCheckBox mcb = impl.cb;
        assertTrue (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        
        click (next);
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("b", impl.cb.getText());
        assertNotSame (impl.cb, mcb);
        
        click (prev);
        assertSame (impl.cb, mcb);
        assertTrue (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("a", impl.cb.getText());
        
        click (impl.cb);
        assertFalse (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());

        click (impl.cb);
        assertTrue (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        
        click (next);
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("b", impl.cb.getText());
        assertFalse (impl.cb.isSelected());
        
        click (impl.cb);
        
        click (next);
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("c", impl.cb.getText());
        
        click (impl.cb);
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertTrue (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("c", impl.cb.getText());
        
        click (prev);
        assertTrue (prev.isEnabled());
        assertTrue (next.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("b", impl.cb.getText());
        
        impl.controller.setBusy(true);
        JButton[] b = getButtons();
        for (int i=0; i < b.length; i++) {
            if (b[i] != help) {
                assertFalse ("All navigation buttons should be enabled when " +
                        "wizard is busy, but " + b[i].getText() + " is enabled", 
                        b[i].isEnabled());
            }
        }
        impl.controller.setBusy(false);
        assertTrue ("SetBusy(false) should restore former state", prev.isEnabled());
        assertTrue ("SetBusy(false) should restore former state", next.isEnabled());
        assertFalse ("SetBusy(false) should restore former state", finish.isEnabled());
        assertTrue ("SetBusy(false) should restore former state", cancel.isEnabled());
        
        click (impl.cb);
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("b", impl.cb.getText());
        
        click (impl.cb);
        click (next);
        
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertTrue (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("c", impl.cb.getText());
        
        click (impl.cb);
        assertFalse (finish.isEnabled());
        assertFalse (next.isEnabled());
        
        click (impl.cb);
        assertFalse (next.isEnabled());
        assertTrue (finish.isEnabled());
        
        click (finish);
        assertFalse (impl.cb.isShowing());
        assertTrue (impl.finished);
    }

//    public void testManual() throws Exception {
//        PanelProviderImpl impl = new PanelProviderImpl();
//        Wizard wiz = impl.createWizard();
//        show (wiz);
//        Thread.currentThread().sleep (40000);
//    }
    
    public void testProblemDisappearsOnBackButton() throws Exception {
        System.out.println("testProblemDisappearsOnBackButton");
        PanelProviderImpl impl = new PanelProviderImpl();
        Wizard wiz = impl.createWizard();
        show (wiz);
        
        while (!impl.active) {
            Thread.currentThread().sleep (200);
        }
        
        JButton next = DefaultWizardDisplayer.buttons[0];
        JButton prev = DefaultWizardDisplayer.buttons[1];
        JButton finish = DefaultWizardDisplayer.buttons[2];
        JButton cancel = DefaultWizardDisplayer.buttons[3];
        
        impl.assertCurrent("a");
        assertFalse (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        
        click (impl.cb);
        JCheckBox mcb = impl.cb;
        assertTrue (next.isEnabled());
        assertFalse (prev.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        
        click (next);
        impl.assertCurrent("b");
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertFalse (finish.isEnabled());
        assertTrue (cancel.isEnabled());
        assertEquals ("b", impl.cb.getText());
        assertNotSame (impl.cb, mcb);
        click(impl.cb);
        assertTrue(next.isEnabled());
        String problem = "Houston, we have a problem...";
        impl.dontResetProblem = true;
        setProblem(problem, impl.controller);
        impl.assertCurrent("b");
        assertTrue (prev.isEnabled());
        assertFalse (next.isEnabled());
        
        click (prev);
        impl.assertCurrent ("a");
        impl.assertRecycledId("a");
        assertTrue (next.isEnabled());
        assertFalse (prev.isEnabled());
        
        click (next);
        impl.assertCurrent("b");
        String[] problems = getKnownProblems(impl);
        assertEquals ("Last set problem should still be present " + Arrays.asList(problems), problem, problems[1]);
        
        assertFalse (next.isEnabled());
        assertTrue (prev.isEnabled());
        
        setProblem (null, impl.controller);
        assertTrue (next.isEnabled());
        setForwardNavigation (WizardController.MODE_CAN_CONTINUE_OR_FINISH, impl.controller);
        assertTrue (finish.isEnabled());
        assertTrue (next.isEnabled());
        setProblem ("Uh oh...", impl.controller);
        assertFalse (finish.isEnabled());
        assertFalse (next.isEnabled());
        
        click (prev);
        assertTrue (next.isEnabled());
        
        click (next);
        impl.assertCurrent("b");
        assertFalse (next.isEnabled());
        assertTrue (prev.isEnabled());
        setProblem (null, impl.controller);
        
        assertTrue (next.isEnabled());
        assertTrue (finish.isEnabled());
        
        click (next);
        impl.assertCurrent("c");
        setProblem (null, impl.controller);
        for (int i=0; i < problems.length; i++) {
            assertNull ("All problems should be null but " + i +" is " + problems[i], problems[i]);
        }
        assertFalse (next.isEnabled());
        assertTrue (finish.isEnabled());
        
        setProblem ("Cant do anything", impl.controller);
        
        assertFalse (finish.isEnabled());
        assertFalse (next.isEnabled());
        impl.assertCurrent("c");
        click(prev);
        click(prev);
        impl.assertCurrent("a");
        
        assertTrue (next.isEnabled());
        assertFalse (prev.isEnabled());
        
        impl.controller.setForwardNavigationMode(WizardController.MODE_CAN_FINISH);
        
        assertFalse (prev.isEnabled());
        assertFalse (next.isEnabled());
        assertTrue (finish.isEnabled());
        
    }
    
    public void testReflectionHackWorks() {
        try {
            Field f = WizardPanelProvider.class.getDeclaredField("knownProblems");
        } catch (Exception e) {
            fail ("The field 'knownProblems' on WizardPanelProvider has been " +
                    "deleted.  Please update TrivialWizardFactoryTest to be" +
                    " able to locate the array of known problems.");
        }
    }
    
    private static String[] getKnownProblems (WizardPanelProvider prov) throws Exception {
        Field f = WizardPanelProvider.class.getDeclaredField("knownProblems");
        f.setAccessible(true);
        return (String[]) f.get(prov);
    }
    
    private void setForwardNavigation (final int val, final WizardController ctl) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                ctl.setForwardNavigationMode(val);
            }
        });
    }
    
    private void setProblem (final String problem, final WizardController ctl) throws Exception {
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                ctl.setProblem (problem);
            }
        });
    }
    
    private static void show (final Wizard wiz) {
        show (wiz, null);
    }
    
    private static void show (final Wizard wiz, final Action helpAction) {
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new DefaultWizardDisplayer().show (wiz, null, helpAction);
                }
            });
            Thread.currentThread().sleep (1000);
        } catch (Exception e) {
            fail (e.getMessage());
        }
        
    }
    
    private static void click (final AbstractButton button) {
        try {
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run() {
                    button.doClick();
                }
            });
//            Thread.currentThread().sleep (500);
        } catch (Exception ie) {
            ie.printStackTrace();
            fail ("interrupted");
        }
    }

    
    private static class PanelProviderImpl extends WizardPanelProvider {
        private boolean finished = false;
        private int step = -1;
        WizardController controller = null;

        PanelProviderImpl(java.lang.String title, java.lang.String[] steps, java.lang.String[] descriptions) {
            super(title, steps, descriptions);
        }

        PanelProviderImpl() {
            super("Test Wizard", new String[] {"a", "b", "c"}, new String[] {"Step 1", "Step 2", "Step 3"});
        }

        boolean active = false;
        String currId = null;
        JCheckBox cb = null;
        protected JComponent createPanel(final WizardController controller, final java.lang.String id, final java.util.Map settings) {
            step++;
            this.controller = controller;
            JPanel result = new JPanel();
            result.setLayout (new BorderLayout());
            cb = new JCheckBox (id);
            cb.addActionListener (new ActionListener() {
                public void actionPerformed (ActionEvent ae) {
                    controller.setProblem(cb.isSelected() ? null : "problem");
                    settings.put (id, cb.isSelected() ? Boolean.TRUE : Boolean.FALSE);
                }
            });

            result.add (cb, BorderLayout.CENTER);
            controller.setProblem (Boolean.TRUE.equals (settings.get (id)) ? null : "problem");

            currId = id;
            result.setName (id);
            settings.put (id, Boolean.TRUE);
            SwingUtilities.invokeLater (new Runnable() {
                public void run() {
                    active = true;
                }
            });
            return result;
        }

        protected java.lang.Object finish(java.util.Map settings) {
            finished = true;
            return "finished";
        }

        public void assertCurrent (String id) {
            if (id == null && currId == null) {
                return;
            } else if ((id == null) != (currId == null)) {
                fail ("Non-match: " + id + ", " + currId);
            } else {
                assertEquals (id, currId);
            }
        }

        private JComponent recycled = null;
        private String recycledId = null;
        private Map recycledSettings = null;
        boolean dontResetProblem = false;
        protected void recycleExistingPanel (String id, WizardController controller, Map settings, JComponent panel) {
            recycled = panel;
            recycledId = id;
            currId = id;
            recycledSettings = settings;
            cb = (JCheckBox) panel.getComponents()[0];
            if (!dontResetProblem) {
                controller.setProblem (cb.isSelected() ? null : "problem");
            }
        }

        public void assertRecycledSettingsContains (String key, String value) {
            assertNotNull (recycledSettings);
            assertEquals (value, recycledSettings.get(key));
        }

        public void assertRecycled (JComponent panel) {
            assertNotNull (recycled);
            assertSame (panel, recycled);
        }

        public void assertRecycledId (String id) {
            assertNotNull (recycledId);
            assertEquals (id, recycledId);
        }

        public void clear() {
            recycled = null;
            recycledId = null;
            recycledSettings = null;
        }

        public void assertStep (int step, String msg) {
            assertTrue (msg, step == this.step);
        }

        public void assertFinished (String msg) {
            assertTrue (msg, finished);
        }

        public void assertNotFinished (String msg) {
            assertFalse (msg, finished);
        }
    }    
    
}
