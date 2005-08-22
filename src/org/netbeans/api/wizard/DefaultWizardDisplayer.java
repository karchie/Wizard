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
 * TrivialWizardFactory.java
 *
 * Created on February 22, 2005, 4:42 PM
 */

package org.netbeans.api.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.wizard.MergeMap;
import org.netbeans.modules.wizard.InstructionsPanel;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.Wizard.WizardListener;
import org.netbeans.spi.wizard.WizardException;

/**
 * Default implementation of WizardFactory.
 *
 * @author Tim Boudreau
 */
class DefaultWizardDisplayer extends WizardDisplayer {
    
    //XXX this class was intended to be a trivial example of how one
    //might implement a wizard UI, but it is morphing into a full blown
    //implementation.  Should clean it up at some point, it's rather
    //spaghetti-ish, unlike the rest of the library
    
    DefaultWizardDisplayer() {
    }

    //for unit tests
    static volatile JButton[] buttons;
    
    protected Object show(final Wizard wizard, Rectangle bounds, Action helpAction) {
        final JPanel panel = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max (600, d.width);
                d.height = Math.max (400, d.height);
                return d;
            }
        };
        if (wizard.getAllSteps().length == 0) {
            throw new IllegalArgumentException ("Wizard has no steps"); //NOI18N
        }
        
        final JLabel ttlLabel = new JLabel (wizard.getStepDescription(wizard.getAllSteps()[0]));
        ttlLabel.setBorder (BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 12, 5),
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("textText")))); //NOI18N
        JPanel ttlPanel = new JPanel() {
            public void doLayout() {
                Dimension d = ttlLabel.getPreferredSize();
                if (ttlLabel.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT) {
                    ttlLabel.setBounds (getWidth() - d.width, 0, d.width, d.height);
                } else {
                    ttlLabel.setBounds (0, 0, d.width, d.height);
                }
            }
            
            public Dimension getPreferredSize() {
                return ttlLabel.getPreferredSize();
            }
        };
        ttlPanel.add (ttlLabel);
        Font f = ttlLabel.getFont();
        if (f == null) {
            f = UIManager.getFont("controlFont"); //NOI18N
        }
        if (f != null) {
            f = f.deriveFont(Font.BOLD);
            ttlLabel.setFont(f);
        }
        
        panel.setLayout (new BorderLayout());
        final InstructionsPanel instructions = new InstructionsPanel (wizard);
        
        panel.setMinimumSize (new Dimension (500, 500));
        
        final JButton next = new JButton ("Next >");
        final JButton prev = new JButton ("< Prev");
        final JButton finish = new JButton ("Finish");
        final JButton cancel = new JButton ("Cancel");
        final JButton help = new JButton();
        if (helpAction != null) {
            help.setAction (helpAction);
            if (helpAction.getValue(Action.NAME) == null) {
                help.setText ("Help");
            }
        } else {
            help.setText ("Help");
        }
        
        next.setDefaultCapable(true);
        prev.setDefaultCapable(true);
        
        help.setVisible (helpAction != null);
        
        final JPanel inner = new JPanel();
        inner.setLayout (new BorderLayout());
        inner.add (ttlPanel, BorderLayout.NORTH);
        
        final JLabel problem = new JLabel("  ");
        Color fg = UIManager.getColor ("nb.errorColor"); //NOI18N
        problem.setForeground (fg == null ? Color.BLUE : fg);
        inner.add (problem, BorderLayout.SOUTH);
        problem.setPreferredSize (new Dimension (20,20));
        
        //Use standard default-button-last order on Aqua L&F
        final boolean aqua = "Aqua".equals (
                UIManager.getLookAndFeel().getID()); //NOI18N
        
        JPanel buttons = new JPanel() {
            public void doLayout() {
                Insets ins = getInsets();
                JButton b = aqua ? finish : cancel;
                
                Dimension n = b.getPreferredSize();
                int y = ((getHeight() - (ins.top + ins.bottom))/ 2) - 
                        (n.height / 2);
                int gap = 5;
                int x = getWidth() - (12 + ins.right + n.width);
                
                b.setBounds (x, y, n.width, n.height);

                b = aqua ? next : finish;
                n = b.getPreferredSize();
                x -= n.width + gap;
                b.setBounds (x, y, n.width, n.height);
                
                b = aqua ? prev : next;
                n = b.getPreferredSize();
                x -= n.width + gap;
                b.setBounds (x, y, n.width, n.height);
                
                b = aqua ? cancel : prev;
                n = b.getPreferredSize();
                x -= n.width + gap;
                b.setBounds (x, y, n.width, n.height);
                
                b = help;
                n = b.getPreferredSize();
                x -= n.width + (gap * 2);
                b.setBounds (x, y, n.width, n.height);
            }
        };
        buttons.setBorder (BorderFactory.createMatteBorder (1, 0, 0, 0, 
                UIManager.getColor("textText"))); //NOI18N
        
        buttons.add (prev);
        buttons.add (next);
        buttons.add (finish);
        buttons.add (cancel);
        buttons.add (help);
        
        panel.add (instructions, BorderLayout.WEST);
        panel.add (buttons, BorderLayout.SOUTH);
        panel.add (inner, BorderLayout.CENTER);
        
        final List buttonlist = Arrays.asList(new JButton[] {
            next, prev, finish, cancel
        });
        
        if (Boolean.getBoolean ("TrivialWizardFactory.test")) { //for unit tests //NOI18N
            List blist = new ArrayList (buttonlist);
            blist.add (help);
            DefaultWizardDisplayer.buttons = 
                    (JButton[]) blist.toArray (new JButton[0]);
        }
        
        String first = wizard.getAllSteps()[0];
        final MergeMap settings = new MergeMap (first);
        final JComponent[] centerPanel = new JComponent[] {
            wizard.navigatingTo(first, settings)
        };
        instructions.setCurrentStep (first);
        inner.add (centerPanel[0], BorderLayout.CENTER);
        prev.setEnabled (false);
        next.setEnabled (wizard.getNextStep() != null);
        int fwdNavMode = wizard.getForwardNavigationMode();
        checkLegalNavMode (fwdNavMode);
        
        finish.setEnabled ((fwdNavMode & Wizard.MODE_CAN_FINISH) != 0);
        
        final Object[] result = new Object[] { null };
        
        ActionListener buttonListener = new ActionListener() {
            private void navigateTo (String id) {
                JComponent comp = wizard.navigatingTo (id, settings);
                instructions.setCurrentStep (id);
                ttlLabel.setText(wizard.getStepDescription(id));
                inner.add (comp, BorderLayout.CENTER);
                inner.remove (centerPanel[0]);
                centerPanel[0] = comp;
                inner.invalidate();
                inner.revalidate();
                inner.repaint();
                comp.requestFocus();
                update();
            }
            
            public void actionPerformed (ActionEvent ae) {
                int action = buttonlist.indexOf (ae.getSource());
                JComponent currCenter = centerPanel[0];
                switch (action) {
                    case 0 : //next
                        String nextId = wizard.getNextStep();
                        settings.push(nextId);
                        navigateTo (nextId);
                        
                        break;
                    case 1 : //prev
                        String prevId = wizard.getPreviousStep();
                        settings.popAndCalve();
                        navigateTo(prevId);
                        
                        break;
                    case 2 : //finish
                        try {
                            result[0] = wizard.finish(settings);
                        } catch (WizardException we) {
                            JOptionPane pane = new JOptionPane (we.getLocalizedMessage());
                            pane.setVisible(true);
                            String id = we.getStepToReturnTo();
                            String curr = settings.currID();
                            try {
                                while (id != null && !id.equals(curr)) {
                                    curr = curr = settings.popAndCalve();
                                }
                                settings.push (id);
                                navigateTo(id);
                                return;
                            } catch (NoSuchElementException ex) {
                                throw new IllegalStateException ("Exception " +
                                    "said to return to " + id + " but no such " +
                                    "step found");
                            }
                        }
                        
                        //Note no break
                        
                    case 3 : //cancel
                        Dialog dlg = (Dialog) 
                            ((JComponent) ae.getSource()).getTopLevelAncestor();
                        dlg.setVisible(false);
                        dlg.dispose();
                        break;
                    default : assert false;
                    
                    
                }
                String prob = wizard.getProblem();
                problem.setText (prob == null ? " " : prob);
                if (prob != null && prob.trim().length() == 0) {
                    //Issue 3 - provide ability to disable next w/o 
                    //showing the error line
                    prob = null;
                }
                Border b = prob == null ? BorderFactory.createEmptyBorder (1, 0, 0, 0)
                    : BorderFactory.createMatteBorder (1, 0, 0, 0, problem.getForeground());
                
                Border b1 = BorderFactory.createCompoundBorder (
                        BorderFactory.createEmptyBorder (0, 12, 0, 12), b);
                
                problem.setBorder (b1);
            }
            
            private void update() {
                if (!wizard.isBusy()) {
                    configureNavigationButtons(wizard, prev, next, finish);
                }
            }
        };
        next.addActionListener(buttonListener);
        prev.addActionListener(buttonListener);
        finish.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);
        
        final WizardListener l = new WizardListener() {
            boolean wasBusy = false;
            public void stepsChanged(Wizard wizard) {
                //do nothing
            }
            public void navigabilityChanged(Wizard wizard) {
                if (wizard.isBusy()) {
                    next.setEnabled(false);
                    prev.setEnabled(false);
                    finish.setEnabled(false);
                    cancel.setEnabled(false);
                    panel.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    wasBusy = true;
                    return;
                } else if (wasBusy) {
                    cancel.setEnabled(true);
                    panel.setCursor (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                configureNavigationButtons(wizard, prev, next, finish);

                String prob = wizard.getProblem();
                Border b = prob == null ? BorderFactory.createEmptyBorder (1, 0, 0, 0)
                    : BorderFactory.createMatteBorder (1, 0, 0, 0, problem.getForeground());
                
                Border b1 = BorderFactory.createCompoundBorder (
                        BorderFactory.createEmptyBorder (0, 12, 0, 12), b);
                
                problem.setBorder (b1);
                problem.setText (prob == null ? " " : prob);
            }


        };
        l.stepsChanged(wizard);
        l.navigabilityChanged(wizard);
        wizard.addWizardListener (l);
        
        JDialog dlg;
        Object o = findLikelyOwnerWindow();
        if (o instanceof Frame) {
            dlg = new JDialog((Frame) o);
        } else if (o instanceof Dialog) {
            dlg = new JDialog ((Dialog) o);
        } else {
            dlg = new JDialog();
        }
        dlg.setTitle (wizard.getTitle());
        dlg.getContentPane().setLayout (new BorderLayout());
        dlg.getContentPane().add (panel, BorderLayout.CENTER);
        if (bounds != null) {
            dlg.setBounds(bounds);
        } else {
            dlg.pack();
        }
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        //XXX get screen insets?
        int x = (d.width / 2) - (dlg.getWidth() / 2);
        int y = (d.height / 2) - (dlg.getWidth() / 2);
        dlg.setLocation(x, y);
        
        dlg.setModal (true);
        dlg.getRootPane().setDefaultButton (next);
        dlg.setVisible(true);
        
        return result[0];
    }
    
    private Window findLikelyOwnerWindow() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
    }
    
    private static void checkLegalNavMode (int i) {
        switch (i) {
            case Wizard.MODE_CAN_CONTINUE :
            case Wizard.MODE_CAN_CONTINUE_OR_FINISH :
            case Wizard.MODE_CAN_FINISH :
                return;
            default :
                throw new IllegalArgumentException ("Illegal forward " + //NOI18N
                        "navigation mode: " + i); //NOI18N
        }
    }
    
    private static void configureNavigationButtons(final Wizard wizard, final JButton prev, final JButton next, final JButton finish) {
        final String nextStep = wizard.getNextStep();
        final int fwdNavMode = wizard.getForwardNavigationMode();
        
        checkLegalNavMode (fwdNavMode);
        
        final String problem = wizard.getProblem();
        
        boolean canContinue = (fwdNavMode & Wizard.MODE_CAN_CONTINUE) != 0;
        boolean canFinish =  (fwdNavMode & Wizard.MODE_CAN_FINISH) != 0;
        boolean enableFinish = canFinish && problem == null;
        boolean enableNext = nextStep != null && canContinue && problem == null;
        next.setEnabled (enableNext);
        prev.setEnabled (wizard.getPreviousStep() != null);
        finish.setEnabled (enableFinish);
        JRootPane root = next.getRootPane();
        if (root != null) {
            if (next.isEnabled()) {
                root.setDefaultButton(next);
            } else if (finish.isEnabled()) {
                root.setDefaultButton(finish);
            } else if (prev.isEnabled()) {
                root.setDefaultButton(prev);
            } else {
                root.setDefaultButton(null);
            }
        }
    }    

}
