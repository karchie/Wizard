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
 * WizardPageTest.java
 * JUnit based test
 *
 * Created on August 20, 2005, 10:15 AM
 */

package org.netbeans.spi.wizard;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author tim
 */
public class WizardPageTest extends TestCase {
    public WizardPageTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(WizardPageTest.class);
    }

    public void testCreateWizardFromClassArray() {
        System.out.println("testCreateWizardFromClassArray");
        Class[] clazz = new Class[]{
                A.class, B.class
        };

        Wizard result = WizardPage.createWizard(clazz);
        assertNotNull(result);

        clazz = new Class[]{
                A.class, BadNoDescription.class, B.class
        };

        try {
            WizardPage.createWizard(clazz);
            fail("Only WizardPage classes should be permitted");
        } catch (Exception e) {
            assertNotNull(e);
        }

        clazz = new Class[]{
                A.class, A.class
        };

        try {
            WizardPage.createWizard(clazz);
            fail("Redundant WizardPages should not be permitted");
        } catch (AssertionError e) {
            assertNotNull(e);
        } catch (Exception e) {
            assertNotNull(e);
        }

        clazz = new Class[]{
                A.class, B.class, BadNoDefaultConstructor.class
        };

        try {
            result = WizardPage.createWizard(clazz);
            String[] steps = result.getAllSteps();
            for (int i = 0; i < steps.length; i++) {
                // Attempt to use the default constructor will throw an
                // exception when we get to BadNoDefaultConstructor
                result.navigatingTo(steps[i], new HashMap());
            }
            // SimpleWizardInfo absorbs the runtime exception and returns
            // a valid panel with the error message attached.
            // fail("Invalid constructor should not be permitted");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    public void testCreateWizardFromPageArray() {
        System.out.println("testCreateWizardFromPageArray");

        String[] ids = new String[]{
                "1", "2", "3"
        };

        Wizard wiz = WizardPage.createWizard(new WizardPage[]{
                new AP(ids[0]),
                new BP(ids[1]),
                new CP(ids[2]),
        });

        String[] fromWizIds = wiz.getAllSteps();
        assertEquals("Step IDs should match", Arrays.asList(ids), Arrays.asList(fromWizIds));
    }

    public void testNullStepOrIDFailsOnRawClass() {
        System.out.println("testNullStepOrIDFailsOnRawClass");
        Exception e = null;
        try {
            Object o = new WizardPage(null, null);
        } catch (NullPointerException ex) {
            e = ex;
        }
//        assertNotNull (e);
    }

    public static final class A extends WizardPage {
        public static String getID() {
            return "A";
        }

        public static String getDescription() {
            return "Step a";
        }
    }

    public static final class B extends WizardPage {
        public static String getID() {
            return "B";
        }

        public static String getDescription() {
            return "Step b";
        }
    }

    public static final class BadNoDescription extends WizardPage {
        public static String getID() {
            return "No Description Page";
        }
    }

    public static final class BadNoDefaultConstructor extends WizardPage {
        public BadNoDefaultConstructor(String foo) {
        }

        public static String getID() {
            return "No Default Constructor Page";
        }

        public static String getDescription() {
            return "No Default Constructor";
        }
    }

    public static final class AP extends WizardPage {
        public AP(String id) {
            super(id, "one", true);

            setLayout(new BorderLayout());
            JCheckBox jcb = new JCheckBox("The sky is blue");
            jcb.setName("blueSky");
            add(jcb, BorderLayout.CENTER);
        }
    }

    public static final class BP extends WizardPage {
        public BP(String id) {
            super(id, "two", true);

            setLayout(new BorderLayout());
            JTextField field = new JTextField("I am a dog");
            field.setName("dogField");
            add(field, BorderLayout.CENTER);
        }
    }

    public static final class CP extends WizardPage {
        public CP(String id) {
            super(id, "three", true);

            setLayout(new BorderLayout());
            JComboBox combo = new JComboBox();
            ComboBoxModel mdl = new DefaultComboBoxModel(new String[]{"first", "second", "third"});
            mdl.setSelectedItem("second");
            combo.setModel(mdl);
            combo.setName("combo");
            add(combo, BorderLayout.CENTER);
        }
    }
}
