/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package net.java.dev.wizard.template;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class BranchDesignerTest extends TestCase {
    
    public BranchDesignerTest(String s) {
        super (s);
    }
    
    public void setUp() {
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.getContentPane().setLayout (new BorderLayout());
        jf.getContentPane().add (new BranchDesignerPanel(
                "Select Destination", 
                "Choose Files to Add", 
                "Yawn Three Times", 
                "Go to Barbados, Become a Dog and Eat Pancakes"));
        jf.pack();
        jf.setVisible (true);
    }
    
    public void testX() throws Exception {
        Thread.sleep(120000);
    }
}
