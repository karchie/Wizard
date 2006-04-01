package org.netbeans.spi.wizard;

import java.util.EventListener;


/**
 * Listener which can detect changes in the state of a wizard as the
 * user proceeds.
 */
public interface WizardObserver extends EventListener {
    /**
     * Called when the number or names of the steps of the
     * wizard changes (for example, the user made a choice in one pane which
     * affects the flow of subsequent steps).
     * @param wizard The wizard whose steps have changed
     */
    public void stepsChanged(Wizard wizard);
    
    /**
     * Called when the enablement of the next/previous/finish buttons 
     * change, or the problem text changes.
     * @param wizard The wizard whose navigability has changed
     */
    public void navigabilityChanged(Wizard wizard);

    /**
     * Called whenever the current step changes.
     *
     * @param wizard The wizard whose current step has changed
     */
    public void selectionChanged(Wizard wizard);
}