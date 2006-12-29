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

/*
 * DeferredWizardResult.java
 *
 * Created on September 24, 2006, 3:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.spi.wizard;

import java.util.Map;


/**
 * Object which can be returned from 
 * <code>WizardPage.WizardResultProducer.finish()</code>
 * or <code>WizardPanelProvider.finish()</code>.  A DeferredWizardResult does
 * not immediately calculate its result;  it is used for cases where some
 * time consuming work needs to be performed to compute the result (such as
 * creating files on disk), and a progress bar should be shown until the work
 * is completed.
 *
 * @author Tim Boudreau
 */
public abstract class DeferredWizardResult {
    private boolean canAbort = false;
    
    protected boolean useBusy = false;
    
    /** 
     * Creates a new instance of DeferredWizardResult which cannot be 
     * aborted.
     */
    public DeferredWizardResult() {
    }
    
    /** Creates a new instance of DeferredWizardResult which may or may not
     * be able to be aborted. */
    public DeferredWizardResult (boolean canAbort) {
        this.canAbort = canAbort;
    }
    
    /** 
     * Begin computing the result.  This method is called on a background
     * thread, not the AWT event thread, and computation can immediately begin.
     * Use the progress handle to set progress as the work progresses. 
     * 
     * IMPORTANT: This method MUST call either progress.finished with the result,
     * or progress.failed with an error message.  If this method returns without
     * calling either of those methods, it will be assumed to have failed.
     * 
     * @param settings The settings gathered over the course of the wizard
     * @param progress A handle which can be used to affect the progress bar.
     */
    public abstract void start (Map settings, ResultProgressHandle progress);
    
    /**
     * If true, the background thread can be aborted.  If it is possible to 
     * abort, then the UI may allow the dialog to be closed while the result
     * is being computed.
     */ 
    public boolean canAbort() {
        return canAbort;
    }
    
    /**
     * Abort computation of the result.  This method will usually be called on
     * the event thread, after <code>start()<code> has been called, and before
     * <code>finished()</code> has been called on the <code>ResultProgressHandler</code>
     * that is passed to <code>start()</code>.
     */ 
    public void abort() {
        //do nothing
    }

    public boolean isUseBusy()
    {
        return useBusy;
    }

    /**
     * Indicate "busy" icon is to be used instead of a progress bar.
     * This can be called by the constructor to avoid an initial display of
     * the progress bar.
     *
     * @param useBusy
     */
    public void setUseBusy(boolean useBusy)
    {
        this.useBusy = useBusy;
    }
}
