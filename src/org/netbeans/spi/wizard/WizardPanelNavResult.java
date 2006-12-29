/*
 * WizardPanelNavResult.java       created on Dec 17, 2006
 * 
 * Copyright (c) 2003-2005 Purisma, Inc.  ALL RIGHTS RESERVED.
 * 
 * This file is a valuable trade secret (&quot;Confidential Information&quot;)
 * of Purisma, Inc.  Distribution and use of this file in source or
 * binary form is controlled by the applicable license agreements.
 */
 
package org.netbeans.spi.wizard;

import java.util.Map;

/**
 * Result class for the methods in WizardPanel.
 * 
 * For immediate action, one of the two constantants PROCEED or REMAIN_ON_PAGE
 * should be returned.  Otherwise an instance of a subclass should be returned
 * that computes a Boolean result.
 * 
 * @author stanley@stanleyknutson.com
 */
public abstract class WizardPanelNavResult extends DeferredWizardResult
{
    /**
     * value for procced to next step in the wizard.
     */
    public static WizardPanelNavResult PROCEED = new WPNRimmediate(Boolean.TRUE);
    /**
     * Value to remain on the current page in the wizard
     */
    public static WizardPanelNavResult REMAIN_ON_PAGE = new WPNRimmediate(Boolean.FALSE);
    
    public boolean isDeferredComputation()
    {
        return true;
    }
    
    /*
     * internal class for the constants only
     */
    private final static class WPNRimmediate extends WizardPanelNavResult
    {
        Boolean value;
        
        WPNRimmediate (Boolean v)
        {
            value = v;
        }
        public boolean isDeferredComputation()
        {
            return false;
        }
        
        public boolean equals (Object o)
        {
            if (o instanceof WPNRimmediate && ((WPNRimmediate)o).value.equals(value))
            {
                return true;
            }
            return false;
        }
        
        public void start(Map settings, ResultProgressHandle progress)
        {
            // Should never get here, this is supposed to be immediate!
            throw new RuntimeException("Immediate result was called as deferral!");
        }
        
    }
}

