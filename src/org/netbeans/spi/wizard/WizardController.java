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
 * WizardController.java
 *
 * Created on March 5, 2005, 7:24 PM
 */

package org.netbeans.spi.wizard;

/**
 * Controller which can be used to modify the UI state of a wizard.  Passed
 * as an argument to methods of <code>PanelProvider</code>.  Use this interface
 * to determine whether the Next/Finish buttons should be enabled, and if some
 * problem explanation text should be displayed.
 * <p>
 * If you are implementing this interface, you are probably doing something
 * wrong.  Use instances of this interface passed to 
 * {@link org.netbeans.spi.wizard.WizardPanelProvider#createPanel 
 * WizardPanelProvider.createPanel}.
 *
 * @author Tim Boudreau
 */
public interface WizardController {
    public static final int STATE_CAN_CONTINUE = 1;
    public static final int STATE_CAN_FINISH = 2;
    public static final int STATE_CAN_CONTINUE_OR_FINISH = 
            STATE_CAN_CONTINUE | STATE_CAN_FINISH;
    
    /**
     * Indicate that there is a problem with what the user has (or has not)
     * input, such that the Next/Finish buttons should be disabled until the
     * user has made some change.
     * <p>
     * Pass null to indicate there is no problem;  non-null indicates there is
     * a problem - the passed string should be a localized, human-readable
     * description that assists the user in correcting the situation.
     */
    void setProblem (String value);
    
    /**
     * Set the forward navigation mode - 
     * indicate that the Finish button of the wizard should or should not be enabled 
     * (assuming <code>setProblem</code> has not been called with a non-null
     * value), and whether or not the Next button of the wizard should also
     * be enabled.
     * <p>
     * <code>setFwdNavMode</code> means two different things, depending on the
     * type of wizard.  In a wizard created by a <code>WizardBranchController</code>,
     * it only enables the finish button if the sub-wizard in question is the last
     * in the branching structure;  if it is not, setting <code>canFinish</code>
     * to true indicates that the next steps in the wizard may now be known,
     * and it should try to find the next sub-wizard to continue.
     */
    void setFwdNavMode (int value);
    
    /**
     * Indicate that some sort of background process is happening (presumably
     * a progress bar is being shown to the user) which cannot be interrupted.
     * <i>Calling this menu disables all navigation and the ability to close
     * the wizard dialog.  Use this option with caution and liberal use of
     * <code>finally</code> to reenable navigation.</i>
     */
    void setBusy (boolean busy);
}
