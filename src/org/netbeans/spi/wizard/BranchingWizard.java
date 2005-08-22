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
/*
 * BranchingWizard.java
 *
 * Created on March 4, 2005, 10:56 PM
 */

package org.netbeans.spi.wizard;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.spi.wizard.Wizard.WizardListener;

/**
 * A Wizard with indeterminate branches.  The actual branch decision-making
 * is done by the WizardBranchController passed to the constructor.
 * <p>
 * Wizards with arbitrary numbers of branches can be handled by a 
 * WizardBranchController by returning wizards created by 
 * another WizardBranchController's <code>createWizard()</code> method.
 * <p>
 * One important point: There should be no duplicate IDs between steps of 
 * this wizard.
 *
 * @author Tim Boudreau
 */
final class BranchingWizard implements Wizard {
    final Wizard initialSteps;
    private Wizard subsequentSteps;
    private Wizard activeWizard;
    private final WizardBranchController brancher;
    private WL wl = null;
    
    public BranchingWizard (WizardBranchController brancher) {
        this.brancher = brancher;
        initialSteps = new SimpleWizard(brancher.getBase(), true);
        setCurrent (initialSteps);
    }
    
    private SimpleWizardInfo lastInfo = null;
    
    protected final Wizard createSecondary (Map settings) {
        return brancher.getWizardForStep(currStep, settings);
    }
    
    private void checkForSecondary() {
        if (wizardData == null) {
            return;
        }
        Wizard newSecondary = createSecondary (wizardData);
        if (((subsequentSteps == null) != (newSecondary == null)) || (subsequentSteps != null && !subsequentSteps.equals (newSecondary))) {
            subsequentSteps = newSecondary;
            fireStepsChanged();
        }
    }
    
    public int getForwardNavigationMode() {
        return activeWizard.getForwardNavigationMode();
    }    
    
    private void setCurrent (Wizard curr) {
        if (this.activeWizard == curr) {
            return;
        }
        if (curr == null) {
            throw new NullPointerException ("Can't set current wizard to null");
        }
        if (this.activeWizard != null) {
            this.activeWizard.removeWizardListener (wl);
        }
        this.activeWizard = curr;
        if (wl == null) {
            wl = new WL();
        }
        curr.addWizardListener (wl);
    }
    
    public final boolean isBusy() {
        return activeWizard.isBusy();
    }

    public final Object finish(Map settings) throws WizardException {
        WizardException exc = null;
        try {
            Object result = activeWizard.finish (settings);
            initialSteps.removeWizardListener (wl);
            //Can be null, we allow bail-out with finish mid-wizard now
            if (subsequentSteps != null) {
                subsequentSteps.removeWizardListener (wl);
            }
            return result;
        } catch (WizardException we) {
            exc = we;
            if (we.getStepToReturnTo() != null) {
                initialSteps.addWizardListener (wl);
                //Can be null, we allow bail-out with finish mid-wizard now
                if (subsequentSteps != null) {
                    subsequentSteps.addWizardListener (wl);
                }
            }
            throw we;
        } finally {
            if (exc == null) {
                subsequentSteps = null;
            }
        }
    }

    public final String[] getAllSteps() {
        String[] result;
        if (subsequentSteps == null) {
            String[] bsteps = initialSteps.getAllSteps();
            result = new String[bsteps.length + 1];
            System.arraycopy (bsteps, 0, result, 0, bsteps.length);
            result[result.length-1] = UNDETERMINED_STEP;
        } else {
            String[] bsteps = initialSteps.getAllSteps();
            String[] csteps = subsequentSteps.getAllSteps();
            result = new String[bsteps.length + csteps.length];
            System.arraycopy (bsteps, 0, result, 0, bsteps.length);
            System.arraycopy (csteps, 0, result, bsteps.length,  csteps.length);
        }
        return result;
    }

    public final String getNextStep() {
        String result;
        if (currStep == null) {
            result = getAllSteps() [0];
        } else {
            String[] steps = getAllSteps();
            int idx = Arrays.asList(steps).indexOf(currStep);
            if (idx == -1) {
                throw new IllegalStateException ("Current step not in" + //NOI18N
                        " available steps:  " + currStep + " not in " + //NOI18N
                        Arrays.asList(steps));
            } else {
                if (idx == steps.length - 1) {
                    if (subsequentSteps == null) {
                        result = UNDETERMINED_STEP;
                    } else {
                        result = subsequentSteps.getNextStep();
                    }
                } else {
                    Wizard w = ownerOf (currStep);
                    if (w == initialSteps && idx == initialSteps.getAllSteps().length -1) {
                        checkForSecondary();
                        if (subsequentSteps != null) {
                            result = subsequentSteps.getAllSteps()[0];
                        } else {
                            result = UNDETERMINED_STEP;
                        }
                    } else {
                        result = w.getNextStep();
                    }
                }
            }
        }
        return getProblem() == null ? result : UNDETERMINED_STEP.equals(result) ? result : null;
    }

    public final String getPreviousStep() {
        if (activeWizard == subsequentSteps && subsequentSteps.getAllSteps() [0].equals(currStep)) {
            return initialSteps.getAllSteps()[initialSteps.getAllSteps().length-1];
        } else {
            return activeWizard.getPreviousStep();
        }
    }

    public final String getProblem() {
        return activeWizard.getProblem();
    }

    public final String getStepDescription(String id) {
        Wizard w = ownerOf (id);
        if (w == null) {
            return null;
        }
        return w.getStepDescription(id);
    }
    
    private Wizard ownerOf (String id) {
        if (UNDETERMINED_STEP.equals(id)) {
            checkForSecondary();
            return subsequentSteps;
        }
        if (Arrays.asList(initialSteps.getAllSteps()).contains(id)) {
            return initialSteps;
        } else {
            checkForSecondary();
            return subsequentSteps;
        }
    }

    public final String getTitle() {
        return activeWizard.getTitle();
    }

    private Map wizardData;
    private String currStep = null;
    public final JComponent navigatingTo(String id, Map settings) {
        this.wizardData = settings;
        currStep = id;
        setCurrent (ownerOf (id));
        return activeWizard.navigatingTo(id, settings);
    }

    private Set listeners = Collections.synchronizedSet (new HashSet());
    public final void removeWizardListener(WizardListener listener) {
        listeners.remove (listener);
    }

    public final void addWizardListener(WizardListener listener) {
        listeners.add (listener);
    }
    
    private void fireNavChanged() {
        checkForSecondary();
        WizardListener[] l = (WizardListener[]) listeners.toArray (
                new WizardListener[listeners.size()]);
        for (int i=0; i < l.length; i++) {
            l[i].navigabilityChanged (BranchingWizard.this);
        }
    }
    
    private void fireStepsChanged() {
        WizardListener[] l = (WizardListener[]) listeners.toArray (
                new WizardListener[listeners.size()]);
        for (int i=0; i < l.length; i++) {
            l[i].stepsChanged (BranchingWizard.this);
        }
    }
    
    private class WL implements WizardListener {
        public void stepsChanged(Wizard wizard) {
            fireStepsChanged();
        }
        
        public void navigabilityChanged(Wizard wizard) {
            fireNavChanged();
        }
    }
}
