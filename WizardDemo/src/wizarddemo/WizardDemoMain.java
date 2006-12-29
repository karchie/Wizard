/*
 * WizardDemoMain.java       created on Dec 29, 2006
 * 
 * Copyright (c) 2003-2005 Purisma, Inc.  ALL RIGHTS RESERVED.
 * 
 * This file is a valuable trade secret (&quot;Confidential Information&quot;)
 * of Purisma, Inc.  Distribution and use of this file in source or
 * binary form is controlled by the applicable license agreements.
 */
 
package wizarddemo;

import java.awt.Rectangle;

import javax.swing.UIManager;

import org.netbeans.api.wizard.WizardDisplayer;

/**
 * Demo of a wizard that uses arbitrary panels rather than WizardPanel objects.
 * 
 */
public class WizardDemoMain
{
    public static void main (String[] ignored) throws Exception {
        //Use native L&F
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
        
        WizardDisplayer.showWizard (new NewPetWizard().createWizard(), 
                new Rectangle (20, 20, 500, 400));
        System.exit(0);
    }

}

