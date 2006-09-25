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
    
    /** Creates a new instance of DeferredWizardResult which cannot be 
     * aborted.
     */
    public DeferredWizardResult() {
    }
    
    /** Creates a new instance of DeferredWizardResult which may or may not
     * be able to be aborted. */
    public DeferredWizardResult (boolean canAbort) {
        this.canAbort = canAbort;
    }
    
    /** Begin computing the result.  This method is called on a background
     * thread, not the AWT event thread, and computation can immediately begin.
     * Use the progress handle to set progress as the work progresses. 
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
    
    /**
     * A controller for the progress bar shown in the user interface
     */ 
    public static abstract class ResultProgressHandle {
        /** Set the current position and total number of steps.  Note it is
         * inadvisable to be holding any locks when calling this method, as it
         * may immediately update the GUI using 
         * <code>EventQueue.invokeAndWait()</code>.
         * 
         * @param currentStep the current step in the progress of computing the
         * result.
         * @param totalSteps the total number of steps.  Must be greater than
         *  or equal to currentStep.
         */
        public abstract void setProgress (int currentStep, int totalSteps);
        /** Set the current position and total number of steps, and description 
         * of what the computation is doing.  Note it is
         * inadvisable to be holding any locks when calling this method, as it
         * may immediately update the GUI using 
         * <code>EventQueue.invokeAndWait()</code>.         
         * @param description Text to describe what is being done, which can
         *  be displayed in the UI.
         * @param currentStep the current step in the progress of computing the
         *  result.
         * @param totalSteps the total number of steps.  Must be greater than
         *  or equal to currentStep.
         */
        public abstract void setProgress (String description, int currentStep, int totalSteps);
        /**
         * Call this method when the computation is complete, and pass in the 
         * final result of the computation.  The method doing the computation
         * (<code>DeferredWizardResult.start()</code> or something it 
         * called) should exit immediately after calling this method.  If the
         * <code>failed()</code> method is called after this method has been
         * called, a runtime exception may be thrown.
         * @param result the Object which was computed, if any.
         */ 
        public abstract void finished(Object result);
        /**
         * Call this method if computation fails.  The message may be some text
         * describing what went wrong, or null if no description.
         * @param message The text to display to the user. The method 
         * doing the computation (<code>DeferredWizardResult.start()</code> or something it 
         * called).  If the <code>finished()</code> method is called after this
         * method has been called, a runtime exception may be thrown.
         * should exit immediately after calling this method.
         * @param message A description of what went wrong, or null
         * @param canNavigateBack whether or not the Prev button should be 
         *  enabled.
         */ 
        public abstract void failed (String message, boolean canNavigateBack);
    }
}
