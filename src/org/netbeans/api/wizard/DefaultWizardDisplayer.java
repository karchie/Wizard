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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
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
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.wizard.MergeMap;
import org.netbeans.modules.wizard.InstructionsPanel;
import org.netbeans.modules.wizard.NbBridge;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardObserver;

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
    
    private static final class JP extends JPanel {
        //XXX get rid of this
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Math.max (600, d.width);
            d.height = Math.max (400, d.height);
            return d;
        }
    }
    
    protected Object show(final Wizard wizard, Rectangle bounds, Action helpAction) {
        final JPanel panel = new JP();
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
                    ttlLabel.setBounds (getWidth() - d.width, 0, getWidth(), d.height);
                } else {
                    ttlLabel.setBounds (0, 0, getWidth(), d.height);
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
        
        final JButton next = new JButton (
                NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                DefaultWizardDisplayer.class, "Next_>")); //NOI18N
        
        final JButton prev = new JButton (
                NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                DefaultWizardDisplayer.class, "<_Prev")); //NOI18N
        
        final JButton finish = new JButton (
                NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                DefaultWizardDisplayer.class, "Finish")); //NOI18N
        
        final JButton cancel = new JButton (
                NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                DefaultWizardDisplayer.class, "Cancel")); //NOI18N
        
        final JButton help = new JButton();
        if (helpAction != null) {
            help.setAction (helpAction);
            if (helpAction.getValue(Action.NAME) == null) {
                help.setText (NbBridge.getString(
                        "org/netbeans/api/wizard/Bundle",  //NOI18N
                        DefaultWizardDisplayer.class, "Help")); //NOI18N
            }
        } else {
            help.setText (NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                    DefaultWizardDisplayer.class, "Help")); //NOI18N
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
        inner.add (centerPanel[0], BorderLayout.CENTER);
        prev.setEnabled (false);
        next.setEnabled (wizard.getNextStep() != null);
        int fwdNavMode = wizard.getForwardNavigationMode();
        checkLegalNavMode (fwdNavMode);
        
        finish.setEnabled ((fwdNavMode & Wizard.MODE_CAN_FINISH) != 0);
        
        final Object[] result = new Object[] { null };
        final String[] failMessage = new String[] { null };
        
        class ButtonListener implements ActionListener {
            private void navigateTo (String id) {
                JComponent comp = wizard.navigatingTo (id, settings);
                ttlLabel.setText(wizard.getStepDescription(id));
                setInnerComponent (comp);
            }
            
            private void setInnerComponent (JComponent comp) {
                inner.add (comp, BorderLayout.CENTER);
                inner.remove (centerPanel[0]);
                centerPanel[0] = comp;
                inner.invalidate();
                inner.revalidate();
                inner.repaint();
                comp.requestFocus();
                if (!inSummary) {
                    update();
                }
            }
            
            boolean inSummary;
            private void handleSummary (Summary summary) {
                inSummary = true;
                JComponent summaryComp = (JComponent) summary.getSummaryComponent(); //XXX
                if (summaryComp.getBorder() != null) {
                    CompoundBorder b = new CompoundBorder (new EmptyBorder (
                            5,5,5,5), summaryComp.getBorder());
                    summaryComp.setBorder (b);
                }
                setInnerComponent ((JComponent) summaryComp); //XXX
                next.setEnabled(false);
                prev.setEnabled(false);
                cancel.setEnabled(true);
                finish.setEnabled(false);
                ((JDialog) dlg).getRootPane().setDefaultButton(cancel);
                instructions.setInSummaryPage(true);
                cancel.setText(closeString); //NOI18N
                ttlLabel.setText (NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                    DefaultWizardDisplayer.class, "Summary")); //NOI18N
                summaryComp.requestFocus();
            }
            
            class Progress extends DeferredWizardResult.ResultProgressHandle {
                JProgressBar progressBar = new JProgressBar();
                JLabel lbl = new JLabel();
                
                public void setProgress(final int currentStep, final int totalSteps) {
                    Runnable r = new Runnable() {
                        public void run() {
                            if (currentStep > totalSteps || currentStep < 0) {
                                if (currentStep == -1 && totalSteps == -1) {
                                    return;
                                }
                                throw new IllegalArgumentException ("Bad step values: " //NOI18N
                                        + currentStep + " out of " + totalSteps); //NOI18N
                            }
                            if (totalSteps == -1) {
                                progressBar.setIndeterminate(true);
                            } else {
                                progressBar.setIndeterminate(false);
                                progressBar.setMaximum(totalSteps);
                                progressBar.setValue(currentStep);
                            }
                        }
                    };
                    invoke (r);
                }
                
                private void invoke (Runnable r) {
                    if (EventQueue.isDispatchThread()) {
                        r.run();
                    } else {
                        try {
                            EventQueue.invokeAndWait(r);
                        } catch (InvocationTargetException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                public void setProgress(final String description, final int currentStep, final int totalSteps) {
                    Runnable r = new Runnable() {
                       public void run() {
                            lbl.setText(description == null ? " " : description); //NOI18N
                            setProgress (currentStep, totalSteps);
                       } 
                    };
                    invoke (r);
                }

                public void finished(final Object o) {
                    Runnable r = new Runnable() {
                        public void run() {
                            if (o instanceof Summary) {
                                Summary summary = (Summary) o;
                                handleSummary (summary);
                                result[0] = summary.getResult();
                            } else if (deferredResult != null) {
                                result[0] = o;
                                dlg.setVisible(false);
                                dlg.dispose();
                            }
                        }
                    };
                    invoke (r);
                }

                public void failed(final String message, final boolean canGoBack) {
                    failMessage[0] = message;
                    Runnable r = new Runnable() {
                        public void run() {
                            //cheap word wrap
                            JLabel comp = new JLabel ("<html><body>" + message); //NOI18N
                            comp.setBorder (new EmptyBorder (5,5,5,5));
                            setInnerComponent (comp);
                            prev.setEnabled (canGoBack);
                            if (!canGoBack) {
                                cancel.setText(closeString);
                            }
                            ttlLabel.setText (NbBridge.getString(
                                "org/netbeans/api/wizard/Bundle",  //NOI18N
                                DefaultWizardDisplayer.class, "Failed")); //NOI18N

                            next.setEnabled(false);
                            cancel.setEnabled(true);
                            finish.setEnabled(false);
                        }
                    };
                    invoke (r);
                }
            }

            DeferredWizardResult deferredResult;
            private void handleDeferredWizardResult (final DeferredWizardResult r) {
                deferredResult = r;
                centerPanel[0].setEnabled(false);
                final Progress progress = new Progress();
                instructions.add (progress.lbl);
                instructions.add (progress.progressBar);
                instructions.invalidate();
                instructions.revalidate();
                instructions.repaint();
                Runnable run = new Runnable() {
                    public void run() {
                        if (!EventQueue.isDispatchThread()) {
                            try {
                                instructions.setInSummaryPage(true);
                                dlg.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                r.start(settings, progress);
                            } finally {
                                try {
                                    EventQueue.invokeAndWait(this);
                                } catch (InvocationTargetException ex) {
                                    ex.printStackTrace();
                                    return;
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                    return;
                                } finally {
                                    dlg.setCursor (Cursor.getDefaultCursor());
                                }
                            }
                        } else {
                            deferredResult = null;
                            cancel.setEnabled(true);
                            instructions.removeAll();
                            instructions.invalidate();
                            instructions.revalidate();
                            instructions.repaint();
                        }
                    }
                };
                Thread runner = 
                        new Thread(run, "Wizard Background Result Thread " + r); //NOI18N
                finish.setEnabled(false);
                cancel.setEnabled(r.canAbort());
                prev.setEnabled(false);
                next.setEnabled(false);
                runner.start();
            }
            
            final String closeString = NbBridge.getString("org/netbeans/api/wizard/Bundle",  //NOI18N
                DefaultWizardDisplayer.class, "Close");             //NOI18N
            public void actionPerformed (ActionEvent ae) {
                int action = buttonlist.indexOf (ae.getSource());
                if (cancel.getText().equals(closeString)) {
                    action = buttonlist.size();
                }
                switch (action) {
                    case 0 : //next
                        String nextId = wizard.getNextStep();
                        settings.push(nextId);
                        navigateTo (nextId);
                        inSummary = false;
                        break;
                    case 1 : //prev
                        String prevId = wizard.getPreviousStep();
                        settings.popAndCalve();
                        deferredResult = null;
                        navigateTo(prevId);
                        inSummary = false;
                        break;
                    case 2 : //finish
                        try {
                            Object o = wizard.finish(settings);
                            System.err.println("WIZARD FINISH GOT ME A " + o);
                            if (o instanceof DeferredWizardResult) {
                                final DeferredWizardResult r = (DeferredWizardResult) o;
                                handleDeferredWizardResult (r);
                                break;
                            } else if (o instanceof Summary) {
                                handleSummary ((Summary) o);
                                result[0] = ((Summary) o).getResult();
                                break;
                            }
                            result[0] = o;
                        } catch (WizardException we) {
                            JOptionPane pane = new JOptionPane(we.getLocalizedMessage());
                            pane.setVisible(true);
                            String id = we.getStepToReturnTo();
                            String curr = settings.currID();
                            try {
                                while (id != null && !id.equals(curr)) {
                                    curr = settings.popAndCalve();
                                }
                                settings.push(id);
                                navigateTo(id);
                                return;
                            } catch (NoSuchElementException ex) {
                                throw new IllegalStateException ("Exception " + //NOI18N
                                    "said to return to " + id + " but no such " + //NOI18N
                                    "step found"); //NOI18N
                            }
                        }
                        
                        //Note no break
                        
                    case 3 : //cancel
                        if (action != 2 && deferredResult != null && deferredResult.canAbort()) {
                            deferredResult.abort();
                        }
                        if (wizard.cancel(settings)) {
                            Dialog dlg = (Dialog) 
                                ((JComponent) ae.getSource()).getTopLevelAncestor();
                            dlg.setVisible(false);
                            dlg.dispose();
                        }
                        break;
                    case 4 : //finish button as Close button
                        Dialog dlg = (Dialog) 
                            ((JComponent) ae.getSource()).getTopLevelAncestor();
                        dlg.setVisible(false);
                        dlg.dispose();
                        break;
                    default : assert false;
                }
                String prob = wizard.getProblem();
                problem.setText (prob == null ? " " : prob); //NOI18N
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
            
            private Dialog dlg;
            void setDialog (Dialog dlg) {
                this.dlg = dlg;
            }
        };
        final ButtonListener buttonListener = new ButtonListener();
        next.addActionListener(buttonListener);
        prev.addActionListener(buttonListener);
        finish.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);
        
        final WizardObserver l = new WizardObserver() {
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

            public void selectionChanged(Wizard wizard) {
                //do nothing
            }
        };
        l.stepsChanged(wizard);
        l.navigabilityChanged(wizard);
        l.selectionChanged(wizard);
        wizard.addWizardListener(l);
        
        JDialog dlg;
        Object o = findLikelyOwnerWindow();
        if (o instanceof Frame) {
            dlg = new JDialog((Frame) o);
        } else if (o instanceof Dialog) {
            dlg = new JDialog((Dialog) o);
        } else {
            dlg = new JDialog();
        }
        buttonListener.setDialog (dlg);
        
        dlg.setTitle (wizard.getTitle());
        dlg.getContentPane().setLayout (new BorderLayout());
        dlg.getContentPane().add (panel, BorderLayout.CENTER);
        if (bounds != null) {
            dlg.setBounds(bounds);
        } else {
            dlg.pack();
        }
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg.addWindowListener (new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                JDialog dlg = (JDialog) e.getWindow();
                boolean dontClose = false;
                if (!wizard.isBusy()) {
                    try {
                        if (buttonListener.deferredResult != null && buttonListener.deferredResult.canAbort()) {
                            buttonListener.deferredResult.abort();
                        } else if (buttonListener.deferredResult != null && !buttonListener.deferredResult.canAbort()) {
                            dontClose = true;
                            return;
                        }
                    } finally {
                        if (!dontClose && wizard.cancel(settings)) {
                            dlg.setVisible (false);
                            dlg.dispose();
                        }
                    }
                }
            }
        });

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        //XXX get screen insets?
        int x = (d.width - dlg.getWidth()) / 2;
        int y = (d.height - dlg.getHeight()) / 2;
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
    /*
    private static final class LDlg extends JDialog {
        public LDlg() {
            
        }
        public LDlg (Frame frame) {
            super (frame);
        }
        
        public LDlg (Dialog dlg) {
            super (dlg);
        }
        
        public void setVisible (boolean val) {
            if (!val) {
                Thread.dumpStack();
            }
            super.setVisible (val);
        }
    }
     */ 
    
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
