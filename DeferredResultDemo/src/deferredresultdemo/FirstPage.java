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
 * FirstPage.java
 *
 * Created on September 24, 2006, 12:57 PM
 */

package deferredresultdemo;

import java.awt.Component;
import org.netbeans.spi.wizard.WizardPage;

/**
 *
 * @author  Tim Boudreau
 */
public class FirstPage extends WizardPage
{

    public static String USE_BUSY = "useBusy";

    /** Creates new form FirstPage */
    public FirstPage()
    {
        super("one", "First step");
        initComponents();
    }

    public static String getDescription()
    {
        return "Check the checkbox";
    }

    protected String validateContents(Component component, Object event)
    {
        if (!jCheckBox1.isSelected())
        {
            return "Checkbox must be checked";
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //GEN-BEGIN:initComponents
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents()
    {
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridLayout(3, 0));

        jCheckBox2.setText("If checked, computing the result will fail, showing an error message");
        jCheckBox2.setBorder(jCheckBox1.getBorder());
        jCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox2.setName("shouldFail");
        add(jCheckBox2);

        jCheckBox1.setText("Click to enable the Next button");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.setName("checkbox");
        add(jCheckBox1);

        jCheckBox3.setText("Click to 'busy' icon when computing");
        jCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        jCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox3.setName("useBusy");
        add(jCheckBox3);

    }// </editor-fold>//GEN-END:initComponents

    //GEN-BEGIN:variables
    // Variables declaration - do not modify
    private javax.swing.JCheckBox jCheckBox1;

    private javax.swing.JCheckBox jCheckBox2;

    private javax.swing.JCheckBox jCheckBox3;
    // End of variables declaration//GEN-END:variables

}
