/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package net.java.dev.wizard.template;

import java.util.Collections;
import java.util.Map;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

/**
 *
 * @author Tim Boudreau
 */
public class WizardWizard implements WizardResultProducer {
    
    /** Creates a new instance of WizardWizard */
    public WizardWizard() {
    }
    
    public static final Wizard createWizard() {
        return WizardPage.createWizard (new Class[] { 
            WizardInfoPanel.class, 
            StepsForm.class, 
            BranchDesignerPanel.class,
            LongDescriptionsPanel.class,
        }, new WizardWizard());
    }
    
    public Object finish(Map m) throws WizardException {
        System.err.println("FINISH:  " + m);
//        return new WizardFilesCreator ();
        new WizardFilesCreator().start (m, null);
        return Collections.EMPTY_SET;
    }

    public boolean cancel(Map m) {
        System.err.println("CANCEL " + m);
        return true;
    }
}
