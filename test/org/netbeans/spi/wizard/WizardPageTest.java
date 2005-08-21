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

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import junit.framework.*;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;



/**
 *
 * @author tim
 */
public class WizardPageTest extends TestCase {
    
    public WizardPageTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WizardPageTest.class);
        
        return suite;
    }

    public void testCreateWizardFromClassArray() {
        System.out.println("testCreateWizardFromClassArray");
        Class[] clazz = new Class[] {
            A.class, B.class
        };
        
        Wizard result = WizardPage.createWizard(clazz, WizardResultProducer.NO_OP);
        assertNotNull (result);

        Exception e = null;
        
        clazz = new Class[] {
            A.class, BadNoDescription.class, B.class
        };

        try {
            result = WizardPage.createWizard(clazz, WizardResultProducer.NO_OP);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull (e);
        e = null;
        
        clazz = new Class[] {
            A.class, A.class
        };
        try {
            result = WizardPage.createWizard(clazz, WizardResultProducer.NO_OP);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull (e);
        e = null;
        
        clazz = new Class[] {
            A.class, A.class
        };
        try {
            result = WizardPage.createWizard(clazz, WizardResultProducer.NO_OP);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull (e);
        e = null;
        
        clazz = new Class[] {
            A.class, B.class, BadNoDefaultConstructor.class
        };
        try {
            result = WizardPage.createWizard(clazz, WizardResultProducer.NO_OP);
            String[] steps = result.getAllSteps();
            for (int i=0; i < steps.length; i++) {
                //Attempt to use the default constructor will throw an
                //exception when we get to BadNoDefaultConstructor
                result.navigatingTo(steps[i], new HashMap());
            }
        } catch (Exception ex) {
            e = ex;
        };
//        assertNotNull(e);
        e = null;        
    }
    
    public void testCreateWizardFromPageArray() {
        System.out.println("testCreateWizardFromPageArray");
        
        AP ap = new AP();
        BP bp = new BP();
        CP cp = new CP();
        
        Wizard wiz = WizardPage.createWizard (new WizardPage[] {
            ap, bp, cp
        }, WizardResultProducer.NO_OP);
        
        String[] ids = new String[] {
            "1", "2", "3"
        };
        
        String[] fromWizIds = wiz.getAllSteps();
        assertEquals (Arrays.asList(ids), Arrays.asList(fromWizIds));
        
    }
    
    public void testNullStepOrIDFailsOnRawClass() {
        System.out.println("testNullStepOrIDFailsOnRawClass");
        Exception e = null;
        try {
            Object o = new WizardPage (null, null);
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
            return "B";
        }
    }
    
    public static final class BadNoDefaultConstructor extends WizardPage {
        public BadNoDefaultConstructor (String foo) {
            
        }
        
        public static String getID() {
            return "BadBad";
        }
        public static String getDescription() {
            return "I am bad";
        }
    }
    
    public static final class AP extends WizardPage {
        final JCheckBox jcb;
        public AP() {
            super ("1", "one", true);
            setLayout (new BorderLayout());
            jcb = new JCheckBox ("The sky is blue");
            jcb.setName ("blueSky");
            add (jcb, BorderLayout.CENTER);
        }
    }
    
    public static final class BP extends WizardPage {
        final JTextField field;
        public BP() {
            super ("2", "two", true);
            setLayout (new BorderLayout());
            field = new JTextField ("I am a dog");
            field.setName ("dogField");
            add (field, BorderLayout.CENTER);
        }
    }
    
    public static final class CP extends WizardPage {
        final JComboBox combo;
        public CP() {
            super ("3", "three", true);
            setLayout (new BorderLayout());
            combo = new JComboBox ();
            ComboBoxModel mdl = new DefaultComboBoxModel (new String[] { "first", "second", "third"});
            mdl.setSelectedItem("second");
            combo.setModel(mdl);
            combo.setName ("combo");
            add (combo, BorderLayout.CENTER);
        }
    }
}
