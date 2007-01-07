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
package net.java.dev.wizard.template.graph;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Tim Boudreau
 */
public class HintWidget extends Widget implements ActionListener {
    private javax.swing.Timer timer = new javax.swing.Timer (75, this);
    /** Creates a new instance of HintWidget */
    public HintWidget(Scene scene) {
        super (scene);
    }

    int tick = 10;
    private String text = null;
    public void setText (String txt) {
        text = txt;
        tick = 10;
        if (timer.isRunning()) {
            timer.restart();
        } else {
            timer.start();
        }
    }
    
    public float getAlpha() {
        float f = tick;
        float result;
        if (tick < 50) {
            result = (50F - (50F - f)) / 50F;
        } else {
            result = (100F - f) / 50F;
        }
        return Math.min (1.0F, Math.max (0.0F, result));
    }
    
    protected void paintWidget () {
        Graphics2D g = getScene().getGraphics();
        Composite old = g.getComposite();
        float alpha = getAlpha();
        g.setComposite (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                alpha));
        Font f = g.getFont();
        g.setFont (f);
        Rectangle r = new Rectangle();
        r.height = g.getFontMetrics ().getHeight();
        r.width = text == null ? 0 : g.getFontMetrics().stringWidth(text);
        g.setPaint (new Color (180, 180, 240));
        g.fillRect (0, 0, r.width, r.height);
        g.setColor (Color.BLACK);
        g.drawRect (0, 0, r.width, r.height);
        int offy = g.getFontMetrics().getMaxAscent();
        if (text != null) {
            g.drawString (text, 0, offy);
        }
        g.setComposite (old);
    }        
    
    public void actionPerformed(ActionEvent arg0) {
        tick++;
        if (getParentWidget() != null) {
            getScene().getView().repaint();
        } else {
            timer.stop();
        }
        if (tick == 100) {
            timer.stop();
            if (getParentWidget() != null) {
                getParentWidget().removeChild (this);
            }
        }
    }
}
