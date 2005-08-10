/*
 * GerbilSteps.java
 *
 * Created on August 9, 2005, 11:51 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package wizarddemo;

import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 *
 * @author Timothy Boudreau
 */
public class GerbilSteps extends WizardPanelProvider {
    
    /** Creates a new instance of GerbilSteps */
    public GerbilSteps() {
        super (new String[] { "Finish" }, new String[] { "Finish" });
    }
    
    protected JComponent createPanel(WizardController controller, String id, Map settings) {
        controller.setCanFinish (true);
        controller.setProblem(null);
        return new JLabel ("Sorry, Gerbils are a bit dull");
    }
    
}
