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
import wizarddemo.panels.DogSizePanel;
import wizarddemo.panels.DogTempermentPanel;

/**
 *
 * @author Timothy Boudreau
 */
public class DogLoversSteps extends WizardPanelProvider {
    
    /** Creates a new instance of DogLoversSteps */
    public DogLoversSteps() {
        super (new String[] { "temperment", "size" },
               new String[] { "Select Temperment", "Choose size" });
    }
    
    protected JComponent createPanel(WizardController controller, String id, Map collectedData) {
        switch (indexOfStep(id)) {
            case 0 :
                return new DogTempermentPanel (controller, collectedData);
            case 1 :
                return new DogSizePanel (controller, collectedData);
            default :
                throw new IllegalArgumentException (id);
        }
    }
    
}
