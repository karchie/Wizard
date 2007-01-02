/*
 * Main.java
 *
 * Created on January 2, 2007, 11:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package customcomponentlisteningdemo;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.WizardPage;

/**
 *
 * @author Tim Boudreau
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WizardDisplayer.showWizard(WizardPage.createWizard("Custom Listening Demo",
                new WizardPage[] { new PickColorPanel() }));
    }
    
}
