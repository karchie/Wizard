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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.java.dev.wizard.template.model.PageModel;
import net.java.dev.wizard.template.model.PageModel;
import net.java.dev.wizard.template.model.WizardModel;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Tim Boudreau
 */
public class WLayout extends GraphLayout <PageModel, String> {
    
    /** Creates a new instance of WLayout */
    public WLayout() {
    }
    
    protected void performGraphLayout(UniversalGraph graph) {
        ConnectScene s = (ConnectScene) graph.getScene();
        layout (s, graph, Collections.<PageModel>emptyList());
    }

    protected void performNodesLayout(UniversalGraph graph, Collection nodes) {
        ConnectScene s = (ConnectScene) graph.getScene();
        layout (s, graph, nodes);
    }

    public void layout(ConnectScene s, UniversalGraph g, Collection <PageModel> nodes) {
        PageModel root = s.getWizardModel().getRootPage();
        int x = 5;
        int y = 5;
        Set <Integer> usedRows = new HashSet <Integer> ();
        Set <PageModel> seen = new HashSet <PageModel> ();
        handle (usedRows, new HashSet <PageModel> (), root, g, x, y);
    }
    
    private static final int DIST = 150;
    private static final int YDIST = 80;
    
    private void handle (Set <Integer> usedRows, Set<PageModel> seen, PageModel root, UniversalGraph <PageModel, String> g, int x, int y) {
        if (seen.contains (root)) {
            return;
        }
        seen.add (root);
        setResolvedNodeLocation(g, root, new Point (x, y));
        PageModel realRoot = ((ConnectScene) g.getScene()).getWizardModel().getRootPage();
        if (root.isBranchPoint() && root != realRoot) {
//            y += YDIST;
        }
        int ct = 0;
        x += DIST ;
        for (PageModel p : root.getDestinations()) {
            int ypos = y;
            while (usedRows.contains(new Integer (ypos))) {
                ypos += (YDIST * ct);
            }
            handle (usedRows, seen, p, g, x, ypos);
            usedRows.add (new Integer(ypos));
            ct++;
        }
    }
    
    
    public void xlayout(ConnectScene s, UniversalGraph g, Collection <PageModel> nodes) {
        WizardModel mdl = s.getWizardModel();
        PageModel root = mdl.getRootPage();
        if (root == null) {
            return;
        }
        int x = layoutDests (g, root, s, 5, 5, 0);
        int y = 5;
        List <PageModel> l = mdl.getOrphans();
        for (PageModel p : l) {
            Widget w = s.findWidget(p);
            Dimension d = w.getPreferredSize();
            if (d == null) {
                d = new Dimension (100, 100);
            }
//            w.setPreferredBounds (new Rectangle (x, y, d.width, d.height));
            setResolvedNodeLocation(g, p, new Point (x, y));
            y += d.height + GAP;
        }
    }
    private static final int GAP = 100;
    private int layoutDests (UniversalGraph g, PageModel m, ConnectScene s, int x, int y, int maxW) {
        Widget w = s.findWidget (m);
        Dimension d = w.getPreferredSize();
        if (d == null) {
            d = new Dimension (100, 100);
        }
//        w.setPreferredBounds (new Rectangle (x, y, d.width, d.height));
        setResolvedNodeLocation(g, m, new Point (x, y));
        y += d.height + GAP;
        List <PageModel> nexts = m.getDestinations();
        int result = 5;
        int currCol = x + 100;
        if (nexts.size() == 1) {
            x += 100;
            result = layoutDests (g, nexts.iterator().next(), s, currCol, y, Math.max (maxW, d.width));
            currCol += 100;
        } else {
            x += maxW + GAP;
            y = 5;
            for (PageModel p : nexts) {
                result = layoutDests (g, p, s, currCol, y, 0);
                currCol += 100;
            }
        }
        return result;
    }
}
