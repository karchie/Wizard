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

import java.awt.Dimension;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;
import net.java.dev.wizard.template.model.PageModel;
import net.java.dev.wizard.template.model.WizardModel;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Tim Boudreau
 */
public class WizLayout implements Layout {
    private final WizardModel mdl;
    /** Creates a new instance of WizLayout */
    public WizLayout(WizardModel mdl) {
        this.mdl = mdl;
    }

    public void layout(Widget widget) {
        ConnectScene s = (ConnectScene)widget.getScene();
        PageModel root = mdl.getRootPage();
        if (root == null) {
            return;
        }
        int x = layoutDests (root, s, 5, 5, 0);
        int y = 5;
        List <PageModel> l = mdl.getOrphans();
        for (PageModel p : l) {
            Widget w = s.findWidget(p);
            Dimension d = w.getPreferredSize();
            if (d == null) {
                d = new Dimension (30, 30);
            }
            w.setPreferredBounds (new Rectangle (x, y, d.width, d.height));
            y += d.height + GAP;
        }
    }
    private static final int GAP = 5;
    private int layoutDests (PageModel m, ConnectScene s, int x, int y, int maxW) {
        Widget w = s.findWidget (m);
        Dimension d = w.getPreferredSize();
        if (d == null) {
            d = new Dimension (30, 30);
        }
        w.setPreferredBounds (new Rectangle (x, y, d.width, d.height));
        x += d.height + GAP;
        List <PageModel> nexts = m.getDestinations();
        int result = 5;
        if (nexts.size() == 1) {
            result = layoutDests (nexts.iterator().next(), s, x, y, Math.max (maxW, d.width));
        } else {
            x += maxW + GAP;
            y = 5;
            for (PageModel p : nexts) {
                result = layoutDests (p, s, x, y, 0);
            }
        }
        return result;
    }
    
    /*
    public void layout(Widget widget) {
        if (widget.getPreferredSize() == null) {
            System.out.println("punt");
            return;
        }
        ConnectScene s = (ConnectScene) widget.getScene();
        WidgetData d = s.getWidgetData();
        int ix = 0;
        Rectangle bounds = new Rectangle (0, 0, widget.getPreferredSize().width, widget.getPreferredSize().height);
        Set <Widget> done = new HashSet <Widget> ();
        int x = 10;
        int y = 10;
        int cwidth = Integer.MIN_VALUE;
        for (Widget w : d.getAllWidgets()) {
            if (ix == 0) {
                Dimension dim = w.getPreferredSize();
                w.setPreferredBounds(new Rectangle (x, y, dim.width, dim.height));
                x += dim.width + 10;
                done.add (w);
            } else if (!done.contains (w)) {
                Dimension dim = w.getPreferredSize();
                cwidth = Math.max (cwidth, dim.width);
                List <Widget> ws = d.getNextSetOfWidgets(w);
                for (Widget ww : ws) {
                    ww.setPreferredBounds (new Rectangle (x, y, dim.width, dim.height));
                }
                y += cwidth + 10;
                cwidth = Integer.MIN_VALUE;
            }
            done.add (w);
            ix++;
        }
    }
     */ 

    public boolean requiresJustification(Widget widget) {
        return false;
    }

    public void justify(Widget widget) {
        //do nothing
    }

}
