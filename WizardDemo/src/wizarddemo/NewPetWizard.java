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

import java.awt.Rectangle;
import java.util.Map;
import javax.swing.UIManager;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import wizarddemo.panels.SpeciesPanel;


/**
 * This is the main entry point, from which the wizard is created.
 *
 * @author Timothy Boudreau
 */
public class NewPetWizard extends WizardBranchController {

    NewPetWizard(  ) {
        super( new InitialSteps(  ) );
    }
    
    public static void main (String[] ignored) throws Exception {
        //Use native L&F
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
        
        WizardDisplayer.showWizard (new NewPetWizard().createWizard(), 
                new Rectangle (20, 20, 500, 400));
        System.exit(0);
    }

    protected WizardPanelProvider getPanelProviderForStep(String step, Map collectedData) {
        //There's only one branch point, so we don't need to test the
        //value of step
        Object species = collectedData.get(SpeciesPanel.KEY_SPECIES);
        if (SpeciesPanel.VALUE_CAT.equals(species)) {
            return getCatLoversSteps();
        } else if (SpeciesPanel.VALUE_DOG.equals(species)) {
            return getDogLoversSteps();
        } else if (SpeciesPanel.VALUE_GERBIL.equals(species)) {
            return null;//new GerbilSteps();
        } else {
            return null;
        }
    }

    private WizardPanelProvider getDogLoversSteps() {
        if (dogLoversSteps == null) {
            dogLoversSteps = new DogLoversSteps();
        }
        return dogLoversSteps;
    }

    private WizardPanelProvider getCatLoversSteps() {
        if (catLoversSteps == null) {
            catLoversSteps = new CatLoversSteps();
        }
        return catLoversSteps;
    }
    
    private CatLoversSteps catLoversSteps = null;
    private DogLoversSteps dogLoversSteps = null;
}
