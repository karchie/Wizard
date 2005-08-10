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

package wizarddemo;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import wizarddemo.panels.CatBreedPanel;
import wizarddemo.panels.CatHairLengthPanel;

/**
 *
 * @author Timothy Boudreau
 */
public class CatLoversSteps extends WizardPanelProvider {
    
    /** Creates a new instance of CatLoversSteps */
    public CatLoversSteps() {
        super (
            new String[] { "hairLength", "breed" }, 
            new String[] { "Select hair length", "Choose breed" });
    }
    
    protected JComponent createPanel(WizardController controller, String id, Map settings) {
        switch (indexOfStep(id)) {
            case 0 :
                return new CatHairLengthPanel (controller, settings);
            case 1 :
                return new CatBreedPanel (controller, settings);
            default :
                throw new IllegalArgumentException (id);
        }
    }
    
}
