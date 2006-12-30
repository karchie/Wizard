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
 * InstructionsPanel.java
 *
 * Created on March 4, 2005, 8:59 PM
 */

package org.netbeans.modules.wizard;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardObserver;

/**
 * A panel that displays a background image and optionally instructions
 * from a wizard, tracking the selected panel and showing that in bold.
 * <p>
 * <b><i><font color="red">This class is NOT AN API CLASS.  There is no
 * commitment that it will remain backward compatible or even exist in the
 * future.  The API of this library is in the packages <code>org.netbeans.api.wizard</code>
 * and <code>org.netbeans.spi.wizard</code></font></i></b>.
 *
 * @author Tim Boudreau
 */
public class InstructionsPanel extends JComponent implements WizardObserver {
    private final BufferedImage img;
    private final Wizard wizard;
    private static final int MARGIN = 5;

    public InstructionsPanel (Wizard wiz) {
        this (null, wiz);
        Font f = UIManager.getFont ("Tree.font"); //NOI18N
        if (f != null) {
            setFont (f);
        }
    }
    
    public void addNotify() {
        super.addNotify();
        wizard.addWizardObserver (this);
    }
    
    public void removeNotify() {
        wizard.removeWizardObserver (this);
        super.removeNotify();
    }
    
    BufferedImage getImage() { //for unit test
        return img;
    }
    
    public InstructionsPanel(BufferedImage img, Wizard wizard) {
        if (img == null) {
            //In the event of classloader issues, also have a way to get
            //the image from UIManager - slightly more portable for large
            //apps
            img = (BufferedImage) UIManager.get ("wizard.sidebar.image"); //NOI18N
        }
        
        String imgStr = System.getProperty("wizard.sidebar.image"); //NOI18N
        //image has not been loaded and user wishes to supply their own image
        if (img == null && imgStr != null) {
            //get an URL, works for jars
            ClassLoader cl = this.getClass().getClassLoader();
            URL url = cl.getResource(imgStr);
            //successfully parsed the URL
            if(url != null){
                try {
                    img = ImageIO.read(url);
                } catch (IOException ioe) {
                    System.err.println("Could not load wizard image " + //NOI18N
                            ioe.getMessage());
                    System.setProperty("wizard.sidebar.image", null); //NOI18N
                    img = null; //error loading img, set to null to use default
                }
            } else { //URL was not successfully parsed, set img to null to use default
                System.err.println("Bad URL for wizard image " + imgStr); //NOI18N
                System.setProperty("wizard.sidebar.image", null); //NOI18N
                img = null;
            }
        }
        if (img == null) {
            try {
                img = ImageIO.read(InstructionsPanel.class.getResourceAsStream(
                        "defaultWizard.png")); //NOI18N
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        this.img = img;
        this.wizard = wizard;
    }

    
    public boolean isOpaque() {
        return img != null;
    }
    
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Font f = getFont() != null ? getFont() : UIManager.getFont("controlFont"); //NOI18N
        FontMetrics fm = g.getFontMetrics (f);
        Insets ins = getInsets();
        if (img != null) {
            int dx = ins.left;
            int dy = ins.top;
            int w = getWidth() - (ins. left + ins.right);
            int h = getHeight() - (ins.top + ins.bottom);
            
            g2d.drawImage(img, dx, dy, w, h, this);
        } else {
            g.setColor (Color.WHITE);
            g.fillRect (ins.left, ins.top, getWidth() - (ins.left + ins.right), getHeight() - (ins.top + ins.bottom));
        }
        String currentStep = wizard.getCurrentStep();
        String[] steps = wizard.getAllSteps();
        int y = fm.getMaxAscent() + ins.top + MARGIN;
        int x = ins.left + MARGIN;
        int h = fm.getMaxAscent() + fm.getMaxDescent() + 3;
        
        Font boldFont = f.deriveFont (Font.BOLD);
        
        g.setFont (boldFont);
        g.drawString (NbBridge.getString ("org/netbeans/modules/wizard/Bundle", //NOI18N
                InstructionsPanel.class, "Steps"), x, y); //NOI18N
        
        int underlineY = ins.top + MARGIN + fm.getAscent() + 3;
        g.drawLine (x, underlineY, x + (getWidth() - (x + ins.left + MARGIN)), 
                underlineY);
        
        y += h + 10;
        g.setFont (getFont());
        
        g.setColor (getForeground());
        for (int i=0; i < steps.length; i++) {
            boolean isUndetermined = Wizard.UNDETERMINED_STEP.equals(steps[i]);
            boolean canOnlyFinish = wizard.getForwardNavigationMode() ==
                    Wizard.MODE_CAN_FINISH;
            if (isUndetermined && canOnlyFinish) {
                break;
            }
            String curr = (i + 1) + ". " + (isUndetermined ?
                NbBridge.getString("org/netbeans/modules/wizard/Bundle",  //NOI18N
                InstructionsPanel.class, "elipsis") :  //NOI18N
                wizard.getStepDescription(steps[i])); //NOI18N
            if (curr != null) {
                boolean selected = steps[i].equals (currentStep);
                if (selected && !inSummaryPage) {
                    g.setFont (boldFont);
                }
                
                int width = fm.stringWidth(curr);
                while (width > getWidth() - (ins.left + ins.right) && curr.length() > 5) {
                    curr = curr.substring(0, curr.length() - 5) + 
                            NbBridge.getString(
                                "org/netbeans/modules/wizard/Bundle", //NOI18N
                                InstructionsPanel.class, "elipsis"); //NOI18N
                }
                
                g.drawString (curr, x, y);
                if (selected) {
                    g.setFont (f);
                }
                y += h;
            }
        }
    }
    
    private int historicWidth = Integer.MIN_VALUE;
    public Dimension getPreferredSize() {
        Font f = getFont() != null ? getFont() : 
            UIManager.getFont("controlFont"); //NOI18N
        
        Graphics g = getGraphics();
        if (g == null) {
            g = new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
        }
        f = f.deriveFont (Font.BOLD);
        FontMetrics fm = g.getFontMetrics(f);
        Insets ins = getInsets();
        int h = fm.getHeight();
        
        String[] steps = wizard.getAllSteps();
        int w = Integer.MIN_VALUE;
        for (int i=0; i < steps.length; i++) {
            String desc = i + ". " + (Wizard.UNDETERMINED_STEP.equals(steps[i]) ?
                NbBridge.getString ("org/netbeans/modules/wizard/Bundle",  //NOI18N
                InstructionsPanel.class, "elipsis") :  //NOI18N
                wizard.getStepDescription(steps[i]));
            if (desc != null) {
                w = Math.max (w, fm.stringWidth(desc) + MARGIN);
            }
        }
        if (Integer.MIN_VALUE == w) {
            w = 250;
        }
        if (img != null) {
            w = Math.max (w, img.getWidth());
        }
        //Make sure we can grow but not shrink
        w = Math.max (w, historicWidth);
        historicWidth = w;
        return new Dimension (w,  ins.top + ins.bottom + ((h + 3) * steps.length));
    }
    
    private boolean inSummaryPage;
    public void setInSummaryPage (boolean val) {
        this.inSummaryPage = val;
        repaint();
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public void stepsChanged(Wizard wizard) {
        repaint();
    }
    
    public void navigabilityChanged (Wizard wizard) {
        //do nothing
    }

    public void selectionChanged(Wizard wizard) {
        repaint();
    }
    
    public void doLayout() {
        Component[] c = getComponents();
        Insets ins = getInsets();
        int y = getHeight() - (MARGIN + ins.bottom);
        int x = MARGIN + ins.left;
        int w = getWidth() - ((MARGIN * 2) + ins.left + ins.right);
        if (w < 0) w = 0;
        for (int i=c.length-1; i >= 0; i--) {
            Dimension d = c[i].getPreferredSize();
            c[i].setBounds (x, y - d.height, w, d.height);
            y -= d.height;
        }
    }
}
