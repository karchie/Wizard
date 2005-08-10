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

import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import wizarddemo.panels.SpeciesPanel;


/**
 * Defines the first two panes of the wizard.  The second one is where the
 * user decides what comes next.
 *
 * @author Timothy Boudreau
 */
 class InitialSteps extends WizardPanelProvider {
    private static final String ANIMAL_LOVER = "animalLover";
    private static final String WHICH_ANIMAL = "whichAnimal";
    private static final String STEP_0_PROBLEM = "Only animal lovers can complete this wizard";

    /**
     * Creates a new instance of InitialSteps
     */
    InitialSteps () {
        super( "New Pet Wizard", new String[] { ANIMAL_LOVER, WHICH_ANIMAL },
            new String[] { "Select basic preferences", "Choose a species" } );
    }

    protected JComponent createPanel (final WizardController controller,
        final String id, final Map data) {
        
        switch ( indexOfStep( id ) ) {
            
            case 0 :

                JPanel result = new JPanel(  );
                result.setLayout( new BorderLayout(  ) );

                final JCheckBox checkbox = new JCheckBox( "I am an animal lover" );
                
                checkbox.addActionListener( new ActionListener(  ) {
                        public void actionPerformed( ActionEvent ae ) {
                            if ( checkbox.isSelected(  ) ) {
                                controller.setProblem( null );
                            } else {
                                controller.setProblem( STEP_0_PROBLEM );
                            }
                        }
                    } );

                result.add ( checkbox );

                controller.setProblem( STEP_0_PROBLEM );
                return result;
                
            case 1 :
                return new SpeciesPanel ( controller, data );

            default :
                throw new IllegalArgumentException ( id );
        }
    }
}
