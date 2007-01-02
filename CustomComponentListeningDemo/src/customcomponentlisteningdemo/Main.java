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

package customcomponentlisteningdemo;

import java.awt.Color;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import net.java.dev.colorchooser.ColorChooser;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

/**
 *
 * @author Tim Boudreau
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
        
        WizardDisplayer.showWizard(WizardPage.createWizard("Custom Listening Demo",
                new WizardPage[] { new PickColorPanel() }, new WRP()));
    }
    
    private static class WRP implements WizardResultProducer {
        public Object finish(Map wizardData) throws WizardException {
            Color c = (Color) wizardData.get("color");
            JLabel lbl = new JLabel ("You chose " + ColorChooser.colorToString(c));
            lbl.setOpaque (true);
            lbl.setBackground (c);
            int avg = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
            if (avg <= 128) {
                lbl.setForeground (Color.WHITE);
            }
            Summary s = Summary.create(lbl, c);
            return s;
        }

        public boolean cancel(Map settings) {
            int result = JOptionPane.showConfirmDialog(null, "Really cancel?", "Cancel", 
                    JOptionPane.OK_CANCEL_OPTION);
            return result == JOptionPane.OK_OPTION;
        }
    }
}
