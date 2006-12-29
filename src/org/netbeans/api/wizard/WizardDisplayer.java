/*  The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.
    You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.
    When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]" */
package org.netbeans.api.wizard;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import javax.swing.Action;

import org.netbeans.api.wizard.displayer.WizardDisplayerImpl;
import org.netbeans.modules.wizard.NbBridge;
import org.netbeans.spi.wizard.Wizard;


/**
 * API to show a Wizard in a dialog.
 * 
 * Alternative is to directly create a WizardDisplayerImpl and invoke its show method.
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
     * @param wizard The wizard to show.  Must not be null
     * @param rect The rectangle on screen for the wizard, may be null for default size
     * @param help An action to invoke if the user presses the help button
     * @param initialProperties are the initial values for properties to be shown
     * and entered in the wizard.  May be null.
     */
    public static Object showWizard (Wizard wizard, Rectangle rect, Action help, Map initialProperties) {
       // assert nonBuggyWizard (wizard);
        // validate it
        nonBuggyWizard (wizard);

        WizardDisplayer factory = NbBridge.getFactoryViaLookup();
        if (factory == null) {
            String wdProp = System.getProperty (SYSPROP_KEY);
            if (wdProp != null) {
                try {
                    factory = (WizardDisplayer) 
                            Class.forName (wdProp).newInstance();
                } catch (Exception e) {
                    System.err.println("Could not instantiate " + wdProp);
                    System.setProperty (SYSPROP_KEY, null);
                    e.printStackTrace();
                }
            }
        }
        
        if (factory == null) {
            factory = // new DefaultWizardDisplayer();
                new WizardDisplayerImpl();
        }
        
        return factory.show (wizard, rect, help, initialProperties);
    }
    
    /** Show a wizard with default window placement and no Help button */
    public static Object showWizard (Wizard wizard) {
        return showWizard (wizard, null, null, null);
    }
    
    /** Show a wizard with default window placement, showing the help button,
     * which will invoke the passed action.
     * @param wizard The wizard to show
     * @param help An action to invoke if the user presses the help button
     * @return The result of Wizard.finish()
     */
    public static Object showWizard (Wizard wizard, Action help) {
        return showWizard (wizard, null, help, null);
    }
    
    /** Show a wizard in the passed location on screen with no help button 
     * @param wizard The wizard to show
     * @param r The rectangle on screen for the wizard
     * @return The result of Wizard.finish()
     */
    public static Object showWizard (Wizard wizard, Rectangle r) {
        return showWizard (wizard, r, null, null);
    }
    
    /**
     * Show a wizard.
     * @param wizard the Wizard to show
     * @param r the bounding rectangle for the wizard dialog on screen, null means "computed from first panel size"
     * @param help An action to be called if the Help button is pressed
     * @param initialProperties are used to set initial values for screens within the wizard.
     * This may be null.
     * @return Whatever object the wizard returns from its <code>finish()</code>
     *  method, if the Wizard was completed by the user.
     */
    protected abstract Object show (Wizard wizard, Rectangle r, Action help, Map initialProperties);
    
    
    private static boolean nonBuggyWizard (Wizard wizard) {
        String[] s = wizard.getAllSteps();
        // assert new HashSet(Arrays.asList(s)).size() == s.length;
        // PURISMA:  replace assert with runtime exception
        if (new HashSet(Arrays.asList(s)).size() != s.length)
        {
            throw new RuntimeException ("steps are duplicated");
        }
        if (s.length == 1 && Wizard.UNDETERMINED_STEP.equals(s[0])) {
           // assert false : "Only ID may not be UNDETERMINED_ID"; //NOI18N
            throw new RuntimeException ("Only ID may not be UNDETERMINED_ID");
        }
        for (int i=0; i < s.length; i++) {
            if (Wizard.UNDETERMINED_STEP.equals(s[i]) && i != s.length - 1) {
             //  assert false :  "UNDETERMINED_ID may only be last element in" + //NOI18N
               //        " ids array " + Arrays.asList(s); //NOI18N
                throw new RuntimeException ( "UNDETERMINED_ID may only be last element in" + //NOI18N
                                             " ids array " + Arrays.asList(s)); //NOI18N)
            }
        }
        return true;
    }

    
}
