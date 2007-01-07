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
 * Microsystems, Inc. All Rights Reserved. */

package net.java.dev.wizard.template.graph;
import java.awt.Point;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.widget.Widget;

final class MultiMoveAction extends WidgetAction.LockedAdapter {

    private MoveStrategy strategy;
    private MoveProvider provider;

    private Widget movingWidget = null;
    private Point dragSceneLocation = null;
    private Point originMultiMoveActionation = null;
    private Point originalSceneLocation;

    public MultiMoveAction (MoveStrategy strategy, MoveProvider provider) {
        this.strategy = strategy;
        this.provider = provider;
    }
    
    public MultiMoveAction () {
        this (ActionFactory.createFreeMoveStrategy(),
                ActionFactory.createDefaultMoveProvider());
    }

    protected boolean isLocked () {
        return movingWidget != null;
    }

    List <Widget> selected;
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (event.isShiftDown()) {
            return State.REJECTED;
        }
        if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 1) {
            ConnectScene scene = (ConnectScene) widget.getScene();
            selected = scene.getSelectedWidgets();
            if (!selected.contains(widget)) {
                if (event.isControlDown()) {
                    List old = selected;
                    selected = new ArrayList <Widget> (selected.size() + 1);
                    selected.addAll (old);
                    selected.add (widget);
                    scene.setSelectedObjects (new HashSet <Widget> (selected));
                    scene.repaint();
                } else {
                    selected.clear();
                    scene.setSelectedObjects(Collections.singleton(scene.findObject(widget)));
                    scene.setFocusedWidget(widget);
                }
            }
            scene.repaint();
            movingWidget = widget;
            originalSceneLocation = widget.getPreferredLocation ();
            if (originalSceneLocation == null)
                originalSceneLocation = new Point ();
            dragSceneLocation = widget.convertLocalToScene (event.getPoint ());
            provider.movementStarted (widget);
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        boolean state = move (widget, event.getPoint ());
        if (state) {
            movingWidget = null;
            provider.movementFinished (widget);
            selected = null;
            lastLoc = null;
        } else {
            ConnectScene scene = (ConnectScene) widget.getScene();
            scene.setSelectedObjects(Collections.singleton(scene.findObject(widget)));
            scene.repaint();
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    Point lastLoc;
    private boolean move (Widget widget, Point newLocation) {
        if (movingWidget != widget) {
            return false;
        }
        ConnectScene scene = (ConnectScene) widget.getScene();
        newLocation = widget.convertLocalToScene (newLocation);
        Point location = new Point (originalSceneLocation.x + newLocation.x - dragSceneLocation.x, originalSceneLocation.y + newLocation.y - dragSceneLocation.y);
        provider.setNewLocation (widget, strategy.locationSuggested (widget, originalSceneLocation, location));
        
        if (lastLoc != null) {
            int offx = location.x - lastLoc.x;
            int offy = location.y - lastLoc.y;
            System.err.println("Got " + selected);
            for (Widget w : selected) {
                if (w == widget) {
                    continue;
                }
                Point p = w.getPreferredLocation();
                if (p == null) {
                    p = new Point();
                }
                Point orig = new Point (p);
                p.x += offx;
                p.y += offy;
                provider.setNewLocation (w, p);
            }
        }
        scene.validate();
        lastLoc = location;
        return true;
    }
}
