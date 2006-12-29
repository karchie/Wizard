/*  The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.
    You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.
    When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]" */

package org.netbeans.spi.wizard;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.wizard.WizardDisplayer;

/**
 * Encapsulates the logic and state of a Wizard.  A Wizard gathers information
 * into a Map, and then performs some action with that information when the
 * user clicks Finish.  To display a wizard, pass it to one of the methods
 * on <code>WizardDisplayer.getDefault()</code>.
 * <p>
 * A Wizard is a series of one or more steps represented
 * by panels in the user interface.  Each step is identified by a unique String ID.
 * Panels are created, usually on-demand, as the user navigates through
 * the UI of the wizard.  Panels typically listen on components they contain
 * and put values into the Map where the wizard gathers data.  Note that if the
 * user navigates <i>backward</i>, data entered on pages after the current one
 * disappears from the Map.
 * <p>
 * To create a Wizard, you do not implement or instantiate this class directly,
 * but rather, use one of the convenience classes in this package.  There are
 * three:
 * <ul>
 * <li><code>WizardPage</code> - use or subclass WizardPage, and pass an array
 * of instances, or an array of the <code>Class</code> objects of your subclasses
 * to <code>WizardPage.createWizard()</code>.  This class offers the added
 * convenience that standard Swing components will be listened to automatically,
 * and if their Name property is set, the value from the component will be
 * automatically put into the settings map. 
 * </li>
 *
 * <li><code>WizardPanelProvider</code> - subclass this to create a Wizard
 * with a fixed set of steps.  You provide a set of unique ID strings to the
 * constructor, for all of the steps in the wizard, and override
 * createPanel() to create the GUI component that should be displayed for
 * each step - it will be called on demand as the user moves through the
 * wizard</li>
 *
 * <li><code>WizardBranchController</code> - this is for creating complex
 * wizards with decision points after which the future steps change, depending
 * on what the user chooses.  Create a simple wizard using WizardPage or
 * WizardPanelProvider to represent the initial steps.
 * Then override <code>getWizardForStep()</code> or
 * <code>getPanelProviderForStep()</code> to return a different Wizard to
 * represent the remaining steps at any point where the set of future steps
 * changes.  You can have as many branch points as you want, simply by
 * using WizardBranchController to create the wizards for different decision
 * points.
 * <p>
 * In other words, a wizard with a different set of panels (or number of steps)
 * depending on the user's decision is really three wizards composed into one -
 * one wizard that provides the initial set of steps, and then two others, one
 * or the other of which will actually provide the steps/panels after the
 * decision point (the Wizards are created on demand, for efficiency, so if
 * the user never changes his or her mind at the decision point, only two
 * of the three Wizards is ever actually created).
 * </li></ul>
 *
 * @see org.netbeans.api.wizard.WizardDisplayer
 * @see WizardPage
 * @see WizardPanelProvider
 * @see WizardBranchController
 *
 * @author Timothy Boudreau
 */
public final class Wizard {
    public static final int MODE_CAN_CONTINUE = 
            WizardController.MODE_CAN_CONTINUE;

    /**
     * Constant that can be returned by <code>getForwardNavigationMode</code> to indicate
     * that the Finish button can be enabled if the problem string is null.
     */
    public static final int MODE_CAN_FINISH =
            WizardController.MODE_CAN_FINISH;
    /**
     * Constant that can be returned by <code>getForwardNavigationMode</code> to indicate
     * that both the Finish and Next buttons can be enabled if the problem 
     * string is null.  This value is a bitmask - i.e. 
     * <code>MODE_CAN_CONTINUE_OR_FINISH == MODE_CAN_CONTINUE | 
     * MODE_CAN_FINISH</code>
     */
    public static final int MODE_CAN_CONTINUE_OR_FINISH = 
            WizardController.MODE_CAN_CONTINUE_OR_FINISH;
    
    /**
     * Special panel ID key indicating a branch point in the wizard,
     * after which the next step(s) are unknown.
     */
    public static final String UNDETERMINED_STEP = "_#UndeterminedStep";
    

    final WizardImplementation impl; //package private for unit tests
    
    /** Creates a new instance of Wizard */
    Wizard(WizardImplementation impl) {
        this.impl = impl;
        if (impl == null) {
            throw new NullPointerException();
        }
    }

    public JComponent navigatingTo(String id, Map wizardData) {
        return impl.navigatingTo(id, wizardData);
    }

    public String getCurrentStep() {
        return impl.getCurrentStep();
    }

    public String getNextStep() {
        return impl.getNextStep();
    }

    public String getPreviousStep() {
        return impl.getPreviousStep();
    }

    public String getProblem() {
        return impl.getProblem();
    }

    public String[] getAllSteps() {
        return impl.getAllSteps();
    }

    public String getStepDescription(String id) {
        return impl.getStepDescription(id);
    }

    public Object finish(Map settings) throws WizardException {
        return impl.finish(settings);
    }

    public boolean cancel (Map settings) {
        return impl.cancel(settings);
    }

    public String getTitle() {
        return impl.getTitle();
    }

    public boolean isBusy() {
        return impl.isBusy();
    }

    public int getForwardNavigationMode() {
        return impl.getForwardNavigationMode();
    }

    private volatile boolean listeningToImpl = false;
    private final List listeners = Collections.synchronizedList (
            new LinkedList());

    private WizardObserver l = null;
    public void addWizardListener(WizardObserver listener) {
        listeners.add(listener);
        if (!listeningToImpl) {
            l = new ImplL();
            impl.addWizardListener(l);
            listeningToImpl = true;
        }
    }

    public void removeWizardListener(WizardObserver listener) {
        listeners.remove(listener);
        if (listeningToImpl && listeners.size() == 0) {
            impl.removeWizardListener(l);
            l = null;
            listeningToImpl = false;
        }
    }

    private class ImplL implements WizardObserver {
        public void stepsChanged(Wizard wizard) {
            WizardObserver[] l = (WizardObserver[]) listeners.toArray(
                    new WizardObserver[listeners.size()]);
            for (int i = 0; i < l.length; i++) {
                l[i].stepsChanged(Wizard.this);
            }
        }

        public void navigabilityChanged(Wizard wizard) {
            WizardObserver[] l = (WizardObserver[]) listeners.toArray(
                    new WizardObserver[listeners.size()]);
            for (int i = 0; i < l.length; i++) {
                l[i].navigabilityChanged(Wizard.this);
            }
        }

        public void selectionChanged(Wizard wizard) {
            WizardObserver[] l = (WizardObserver[]) listeners.toArray(
                    new WizardObserver[listeners.size()]);
            for (int i = 0; i < l.length; i++) {
                l[i].selectionChanged(Wizard.this);
            }
        }
    }

    public int hashCode() {
        return impl.hashCode() * 17;
    }

    public boolean equals (Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Wizard) {
            return impl.equals (((Wizard)o).impl);
        } else {
            return false;
        }
    }

    public void show () {
        WizardDisplayer.showWizard(this);
    }

    public Object show (Wizard wizard, Action help) {
        return WizardDisplayer.showWizard (wizard, help);
    }

    public Object show (Wizard wizard, Rectangle r) {
        return WizardDisplayer.showWizard (wizard, r);
    }

    public Object show (Wizard wizard, Rectangle r, Action help) {
        return WizardDisplayer.showWizard (wizard, r, help, null);
    }
}
