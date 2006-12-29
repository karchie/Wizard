/*
 * LabeledComponent.java       created on Dec 9, 2006
 * 
 * Copyright (c) 2003-2005 Purisma, Inc.  ALL RIGHTS RESERVED.
 * 
 * This file is a valuable trade secret (&quot;Confidential Information&quot;)
 * of Purisma, Inc.  Distribution and use of this file in source or
 * binary form is controlled by the applicable license agreements.
 */
 
package loginWizardDemo;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Show a component with a label above it.
 * @author Stanley@stanleyKnutson.com
 */
public class LabeledComponent extends JPanel
{
    JLabel _label;
    
    JComponent _component;
    
    public LabeledComponent (String label, JComponent component)
    {
        _label = new JLabel(label);
        _component = component;
        
        setLayout(new BorderLayout());
        
        add(_label, BorderLayout.NORTH);
        add(_component, BorderLayout.CENTER);
    }
    
    public JComponent getCenterComponent()
    {
        return _component;
    }

    public JComponent getComponent()
    {
        return _component;
    }

    public void setComponent(JComponent component)
    {
        _component = component;
    }

    public JLabel getLabel()
    {
        return _label;
    }
}

