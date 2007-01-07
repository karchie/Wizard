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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.java.dev.wizard.template.model.PageModel;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau
 */
public class PageWidget extends IconNodeWidget {
    private static final GradientPaint pt = new GradientPaint (0, 20, new Color (220, 180, 255), 0, 22, new Color (240, 240, 240));
    /** Creates a new instance of PageWidget */
    private PageModel page;
    public PageWidget(Scene scene, PageModel page) {
        super (scene);
        this.page = page;
        super.getLabelWidget().setLabel (trim(page.getDescription()));
        super.getImageWidget().setImage(Utilities.loadImage ("net/java/dev/wizard/template/graph/custom_displayable_32.png"));
        setToolTipText(flow (page.getDescription()));
    }
    
    public PageModel getPageModel() {
        return page;
    }
    
    private static String flow (String txt) {
        StringBuilder sb = new StringBuilder("<html>");
        String[] s = txt.split(" ");
        for (int i = 0; i < s.length; i++) {
            sb.append (s[i]);
            if (i % 4 == 0) {
                sb.append ("<br>");
            } else {
                sb.append (' ');
            }
        }
        return sb.toString();
    }
    
    static String trim (String txt) {
        char[] c = txt.toCharArray();
        int max = 24;
        if (c.length > max) {
            c[21] = '.';
            c[22] = '.';
            c[23] = '.';
            return new String (c, 0, 24);
        }
        return txt;
    }
    
    protected void paintWidget () {
        Graphics2D g = getScene().getGraphics();
        boolean hl = super.getState().isSelected();
        g.setPaint (hl ? Color.ORANGE : pt);
        Rectangle r = super.getBounds();
        g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        Color c = super.getForeground();
        g.setPaint (c);
        g.setFont (getFont());
        int pos = g.getFontMetrics().getMaxAscent();
        super.paintWidget();
    }
    
}
