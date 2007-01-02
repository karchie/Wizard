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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import net.java.dev.colorchooser.ColorChooser;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.CustomComponentListener;

/**
 * Demos WizardPanel listening on a custom component.
 *
 * @author Tim Boudreau
 */
public class PickColorPanel extends WizardPage {
    private final JLabel lbl = new JLabel ("Click to choose color, but not black or white");
    private final ColorChooser chooser = new ColorChooser (Color.BLACK);
    
    public PickColorPanel() {
        super ("Step1", "Choose a Color", true);
        setLongDescription("Select a color");
        chooser.setName ("color");
        setLayout (new FlowLayout());
        add (lbl);
        add (chooser);
        setPreferredSize(new Dimension (500, 300));
    }
    
    protected CustomComponentListener createCustomComponentListener() {
        return new CCL();
    }
    
    protected String validateContents(Component component, Object event) {
        if (component == null || component == chooser) {
            Color c = chooser.getColor();
            boolean isBlackOrWhite = Color.BLACK.equals (c) || 
                    Color.WHITE.equals (c);
            return isBlackOrWhite ? "Color may not be black or white" : null;
        }
        return null;
    }
    
    
    private static final class CCL extends CustomComponentListener implements ActionListener {
        private CustomComponentNotifier notifier;
        public boolean accept(Component c) {
            return c instanceof ColorChooser;
        }

        public void startListeningTo(Component c, CustomComponentNotifier n) {
            notifier = n;
            ((ColorChooser) c).addActionListener (this);
        }

        public void stopListeningTo(Component c) {
            ((ColorChooser) c).removeActionListener (this);
        }

        public Object valueFor(Component c) {
            return ((ColorChooser) c).getColor();
        }
        
        public void actionPerformed (ActionEvent e) {
            notifier.userInputReceived((Component) e.getSource(), e);
        }
    }
    
}
