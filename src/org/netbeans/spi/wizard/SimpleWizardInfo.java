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
 * SimpleWizardInfo.java
 *
 * Created on March 4, 2005, 9:46 PM
 */

package org.netbeans.spi.wizard;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Provides information about a simple wizard.  Wraps a 
 * WizardPanelProvider and provides a connection to the instance of
 * SimpleWizard created for it, acting as the WizardController for
 * calls to WizardPanelProvider.createPanel().
 */
final class SimpleWizardInfo implements WizardController {
    private WeakReference wizard = null;
    private final String[] descriptions;
    private final String[] steps;
    final Boolean[] knownCanProceed;
    final int[] knownFwdNavModes;
    private static String UNINIT = "Uninitialized";
    private String problem = UNINIT;
    private final String title;
    private final WizardPanelProvider provider;
    private boolean busy = false;

    SimpleWizardInfo (WizardPanelProvider provider) {
        this (provider.title, provider.steps, provider.descriptions, provider);
    }
    
    /**
     * Create an instance of Info, which will provide panels for a simple,
     * non-branching wizard, passing a localized title, a list of steps
     * and descriptions.
     */
    protected SimpleWizardInfo (String title, String[] steps, String[] descriptions, WizardPanelProvider provider) {
        if (steps == null) {
            throw new NullPointerException ("Null steps");
        }
        if (descriptions == null) {
            throw new NullPointerException ("Null descriptions");
        }
        this.steps = steps;
        this.descriptions = descriptions;
        if (new HashSet(Arrays.asList(steps)).size() < steps.length) {
            throw new IllegalArgumentException ("Duplicate ID: " + Arrays.asList(steps));
        }
        if (descriptions.length != steps.length) {
            if (steps.length != descriptions.length + 1 && !Wizard.UNDETERMINED_STEP.equals(steps[steps.length-1])) {
                throw new IllegalArgumentException ("Steps and descriptions " +
                        "array lengths not equal: " + Arrays.asList(steps) + ":"
                        + Arrays.asList(descriptions));
            }
        }
        knownCanProceed = new Boolean [steps.length];
        knownFwdNavModes = new int [steps.length];
        Arrays.fill (knownFwdNavModes, -1);
        this.title = title;
        this.provider = provider;
    }


    final void setWizard (SimpleWizard wizard) {
        this.wizard = new WeakReference(wizard);
    }

    final SimpleWizard getWizard() {
        return wizard != null ? (SimpleWizard) wizard.get() : null;
    }

    /**
     * Create a panel that represents a named step in the wizard.
     * This method will be called exactly <i>once</i> in the life of 
     * a wizard.  The panel should retain the passed settings Map, and
     * add/remove values from it as the user enters information, calling
     * <code>setProblem()</code> and <code>setCanFinish()</code> as
     * appropriate in response to user input.
     * 
     * @param id The name of the step, as supplied in the constructor
     * @param settings A Map containing settings from earlier steps in
     *   the wizard
     * @return A JComponent
     */
    protected JComponent createPanel (String id, Map settings) {
        try {
            JComponent result = provider.createPanel(this, id, settings);
            if (result instanceof WizardPage) {
                ((WizardPage) result).setController(this);
                ((WizardPage) result).setWizardDataMap(settings);
            }
            return result;
        } catch (RuntimeException re) {
            JTextArea jta = new JTextArea();
            jta.setBorder (BorderFactory.createMatteBorder(2,2,2,2,Color.RED));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream str = new PrintStream(buf);
            re.printStackTrace(str);
            jta.setText (new String(buf.toByteArray()));
            setProblem(re.getLocalizedMessage());
            return new JScrollPane(jta);
        }
    }

    /**
     * Instantiate whatever object (if any) the wizard creates from its
     * gathered data.
     */
    protected Object finish (Map settings) throws WizardException {
        assert canFinish();
        return provider.finish (settings);
    }

    /**
     * The method provides a chance to call setProblem() or setCanFinish() when
     * the user re-navigates to a panel they've already seen - in the case 
     * that the user pressed the Previous button and then the Next button.
     * <p>
     * The default implementation does nothing, which is sufficient for 
     * most implementations.  If whether this panel is valid or not could
     * have changed because of changed data from a previous panel,
     * you may want to override this method to ensure validity and currNavMode
     * are set correctly.
     * <p>
     * This method will <i>not</i> be called when a panel is first instantiated - 
     * <code>createPanel()</code> is expected to set validity and currNavMode
     * appropriately.
     * <p>
     * The settings Map passed to this method will always be the same 
     * Settings map instance that was passed to <code>createPanel()</code>
     * when the panel was created.
     */
    protected void recycleExistingPanel (String id, Map settings, JComponent panel) {
        provider.recycle(id, this, settings, panel);
    }

    private int index() {
        SimpleWizard wizard = getWizard();
        if (wizard != null) {
            return wizard.currentIndex();
        } else {
            return 0;
        }
    }

    public final void setBusy (boolean value) {
        if (value != busy) {
            busy = value;
            fire();
            knownCanProceed[index()] = value ? Boolean.FALSE : Boolean.TRUE;
        }
    }
    
    /**
     * Set whether or not the contents of this panel are valid.  When
     * user-entered information in a panel changes, call this method as
     * appropriate.
     */
    public final void setProblem (String value) {
        this.problem = value;
        int idx = index();
        knownCanProceed[idx] = problem == null ? Boolean.TRUE : Boolean.FALSE;
        provider.setKnownProblem(problem, idx);
        fire();
    }

    private int currNavMode = STATE_CAN_CONTINUE;

    /**
     * Set whether or not the Finish button should be enabled.  Neither 
     * the Finish nor Next buttons will be enabled if setProblem has 
     * been called with non-null.
     * <p>
     * Legal values are: WizardController.STATE_CAN_CONTINUE,
     * WizardController.STATE_CAN_FINISH or 
     * WizardController.STATE_CAN_CONTINUE_OR_FINISH.
     * <p>
     * This method is used to set what means of forward navigation should
     * be available if the current panel is in a valid state (problem is
     * null).  It is <i>not</i> a way to disable both the next button 
     * and the finish button, only a way to choose either or both.
     *
     * @see setProblem
     * @param value The forward navigation mode
     *
     */
    public final void setFwdNavMode (int value) {
        switch (value) {
            case STATE_CAN_CONTINUE :
            case STATE_CAN_FINISH :
            case STATE_CAN_CONTINUE_OR_FINISH :
                break;
            default :
                throw new IllegalArgumentException (Integer.toString(value));
        }
        if (currNavMode != value) {
            currNavMode = value;
            fire();
        }
        knownFwdNavModes[index()] = value;
    }

    final String getTitle() {
        return title;
    }

    final void update() {
        int idx = index();
        boolean change = knownFwdNavModes[idx] != -1 && currNavMode != knownFwdNavModes[idx];
        setProblem (provider.getKnownProblem(idx));
//    System.err.println("RE-NAV THROUGH " + idx + " currNavMode " + currNavMode + " known nave mode is " + knownFwdNavModes[idx] + " change " + change);
        currNavMode = knownFwdNavModes[idx] == -1 ? WizardController.STATE_CAN_CONTINUE : knownFwdNavModes[idx];
        if (change) {
            fire();
        }
    }
    
    final void fire() {
        Wizard wiz = getWizard();
        if (wiz != null) {
            getWizard().fireNavigability();
        }
    }

    final boolean isValid() {
        return getProblem() == null;
    }

    final boolean canFinish() {
        return isValid() && (currNavMode != -1 && (currNavMode & STATE_CAN_FINISH) != 0);
    }
    
    final boolean canContinue() {
//        System.err.println("CurrNavMode now " + currNavMode);
        return isValid() && (currNavMode == -1 || (currNavMode & STATE_CAN_CONTINUE) != 0);
    }

    String[] getDescriptions() {
        return descriptions;
    }

    String[] getSteps() {
        return steps;
    }

    final String getProblem() {
        return problem;
    }
    
    boolean isBusy() {
        return busy;
    }
    
    public boolean equals (Object o) {
        if (o != null && o.getClass() == getClass()) {
            SimpleWizardInfo info = (SimpleWizardInfo) o;
            
            assert info.descriptions != null : "Info.descriptions == null";
            assert info.steps != null : "Info.steps == null";

            return Arrays.equals(info.descriptions, descriptions) &&
                   Arrays.equals(info.steps, steps) &&
                   info.title == title;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        int result = 0;
        for (int i=0; i < steps.length; i++) {
            result += (steps[i].hashCode() * (i+1)) ^ 31;
        }
        return result + title.hashCode();
    }    
}