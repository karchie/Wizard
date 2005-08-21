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

package wizardpagedemo;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

/**
 * Demo entry point.  Assembles a wizard from a set of classes and shows it.
 *
 * @author Tim Boudreau
 */
public class Main {
    public static void main(String[] ignored) {
        //All we do here is assemble the list of WizardPage subclasses we
        //want to show:
        Class[] pages = new Class[] {
            AnimalTypePage.class,
            LocomotionPage.class,
            OtherAttributesPage.class,
            FinalPage.class
        };
        
        //Use the utility method to compose a Wizard
        Wizard wizard = WizardPage.createWizard(pages, WizardResultProducer.NO_OP);
        
        //And show it onscreen
        WizardDisplayer.showWizard (wizard);
        System.exit(0);
    }
    
}
