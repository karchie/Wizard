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
 * WizardFactory.java
 *
 * Created on February 22, 2005, 4:39 PM
 */

package org.netbeans.api.wizard;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.Action;
import org.netbeans.spi.wizard.Wizard;


/**
 * API to show a Wizard in a dialog.
 *
 * @author Tim Boudreau
 */
public abstract class WizardDisplayer {
    protected WizardDisplayer() {
    }
    
    
    private static final String SYSPROP_KEY = "WizardDisplayer.default";
    /**
     * Display a wizard in a dialog, using the default implementation of
     * WizardDisplayer.
     */
    public static Object showWizard (Wizard wizard, Rectangle r, Action help) {
        assert nonBuggyWizard (wizard);
        
//        WizardFactory factory = (WizardFactory) Lookup.getDefault().lookup(
//                WizardFactory.class);
        WizardDisplayer factory = null;
        String wdProp = System.getProperty (SYSPROP_KEY);
        if (wdProp != null) {
            try {
                //XXX use this for now
                factory = (WizardDisplayer) Class.forName (wdProp).newInstance();
            } catch (Exception e) {
                System.err.println("Could not instantiate " + wdProp);
                System.setProperty (SYSPROP_KEY, null);
            }
                
        }
        
        if (factory == null) {
            factory = new DefaultWizardDisplayer();
        }
        
        return factory.show (wizard, r, help);
    }
    
    public static Object showWizard (Wizard wizard) {
        return showWizard (wizard, null, null);
    }
    
    public static Object showWizard (Wizard wizard, Action help) {
        return showWizard (wizard, null, help);
    }
    
    public static Object showWizard (Wizard wizard, Rectangle r) {
        return showWizard (wizard, r, null);
    }
    
    /**
     * Show a wizard.
     * @param wizard the Wizard to show
     * @param r the bounding rectangle for the wizard dialog on screen
     * @param help An action to be called if the Help button is pressed
     * @return Whatever object the wizard returns from its <code>finish()</code>
     *  method, if the Wizard was completed by the user.
     */
    protected abstract Object show (Wizard wizard, Rectangle r, Action help);
    
    
    private static boolean nonBuggyWizard (Wizard wizard) {
        String[] s = wizard.getAllSteps();
        assert new HashSet(Arrays.asList(s)).size() == s.length;
        if (s.length == 1 && Wizard.UNDETERMINED_STEP.equals(s[0])) {
            assert false : "Only ID may not be UNDETERMINED_ID";
        }
        for (int i=0; i < s.length; i++) {
            if (Wizard.UNDETERMINED_STEP.equals(s[i]) && i != s.length - 1) {
               assert false :  "UNDETERMINED_ID may only be last element in" +
                       " ids array " + Arrays.asList(s);
            }
        }
        return true;
    }
}
