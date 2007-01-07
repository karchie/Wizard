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
 * Microsystems, Inc. All Rights Reserved
 */

package net.java.dev.wizard.template.graph;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.java.dev.wizard.template.model.PageModel;
import net.java.dev.wizard.template.model.WizardModel;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 * @author David Kaspar
 */
public class ConnectScene extends GraphScene <PageModel, String> {

    private LayerWidget mainLayer = new LayerWidget (this);
    private LayerWidget connectionLayer = new LayerWidget (this);
    private LayerWidget interractionLayer = new LayerWidget (this);
    private LayerWidget selectionLayer = new LayerWidget (this);
    private HintWidget hintWidget = new HintWidget (this);

    private WidgetAction multiSelectAction = new MultiSelectAction ();
    private WidgetAction connectAction = new WrapperAction (ActionFactory.createConnectAction (interractionLayer, new SceneConnectProvider ()));
    private WidgetAction reconnectAction = ActionFactory.createReconnectAction (new SceneReconnectProvider ());
    private WidgetAction dragAction = new WidgetDragAction();
    private WidgetAction multiMoveAction = new MultiMoveAction();

    private long nodeCounter = 0;
    private long edgeCounter = 0;

    private ConnectScene () {
        addChild (mainLayer);
        
        addChild (connectionLayer);
        addChild (interractionLayer);
        addChild (selectionLayer);
        selectionLayer.setLayout (LayoutFactory.createAbsoluteLayout());

        getActions ().addAction (multiSelectAction);

        setZoomFactor(0.75D);
//        mainLayer.addChild (new LabelWidget (this, "Click on background to create a node. Drag a node to create a connection."));
    }
    
    public WizardModel getWizardModel() {
        return mdl;
    }

    private WizardModel mdl;
    public ConnectScene (String... nodes) {
        this();
        mdl = new WizardModel (nodes);
//        mainLayer.setLayout (new WizLayout(mdl));
        Collection <PageModel> pages = mdl.getPages();
        for (PageModel p : pages) {
            addNode (p);
        }
//            setEdgeSource (edge, source);
//            setEdgeTarget (edge, target);
        for (PageModel p : pages) {
            List <PageModel> dests = p.getDestinations();
            String edgeId = p.getUID() + ".edge";
            Widget edge = addEdge (edgeId);
            setEdgeSource (edgeId, p);
            for (PageModel d : dests) {
                setEdgeTarget (edgeId, d);
            }
        }
        reflow();
    }
    
    public void reflow() {
//        SceneLayout l = LayoutFactory.createSceneGraphLayout(this, new GridGraphLayout());
        SceneLayout l = LayoutFactory.createSceneGraphLayout(this, new WLayout());
        l.invokeLayout();
        validate();
    }
    
    public void initialLayout() {
//        SceneLayout l = LayoutFactory.createDevolveWidgetLayout(mainLayer, LayoutFactory.createVerticalLayout(SerialAlignment.JUSTIFY, 20), true);
//        SceneLayout l = LayoutFactory.createDevolveWidgetLayout(mainLayer, LayoutFactory.createVerticalLayout(SerialAlignment.JUSTIFY, 20), true);
//        l.invokeLayout();
//        validate();
        reflow();
    }
    
    public void addNode (PageModel nd, String connectTo) {
        System.err.println("ADD NDOE " + nd + " connectTo");
        super.addNode (nd);
        String edge = nd.getUID() + ".edge";
        setEdgeSource (edge, nd);
        setEdgeTarget (edge, nd);
    }

    private final GradientPaint pt = new GradientPaint (0, 0, new Color (220, 180, 255), 0, 16, new Color (240, 240, 255));
    protected Widget attachNodeWidget (PageModel node) {
        PageWidget label = new PageWidget (this, node);
        label.setBackground (pt);
        label.getActions ().addAction (createObjectHoverAction ());
        label.getActions ().addAction (createSelectAction ());
//        label.getActions().addAction (clickAction);
//        label.getActions().addAction(ActionFactory.createRectangularSelectAction(this, selectionLayer));
//        label.getActions().addAction (dragAction);
        label.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditorImpl()));
        label.getActions ().addAction (connectAction);
        label.getActions().addAction (multiMoveAction);
        label.getActions().addAction (new DeleteAction());
        addEdge(node + ".edge");
        mainLayer.addChild (label);
        return label;
    }
    
    public List <Widget> getSelectedWidgets() {
        validate();
        List <Widget> widgets = new ArrayList <Widget> (mainLayer.getChildren());
        for (Iterator <Widget> i = widgets.iterator(); i.hasNext();) {
            if (!i.next().getState().isSelected()) {
                i.remove();
            }
        }
        return widgets;
    }
    
    protected Widget attachEdgeWidget (String edge) {
        System.err.println("attach edge widget "+ edge);
        ConnectionWidget connection = new ConnectionWidget (this);
        connection.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        connection.setPaintControlPoints(true);
        connection.setForeground(new Color (128, 128, 0));
        connection.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
        connection.getActions ().addAction (createObjectHoverAction ());
        connection.getActions ().addAction (createSelectAction ());
        connection.getActions ().addAction (reconnectAction);
        connectionLayer.addChild (connection);
        return connection;
    }
    
    public WidgetData getWidgetData() {
        Collection <String> edges = getEdges();
        List <Widget> widgets = new ArrayList <Widget> (mainLayer.getChildren());
        Map <Widget, List <Widget>> m = 
                new HashMap <Widget, List <Widget>> ();
        Map <Widget, Set <Connection>> cs = new HashMap <Widget, Set <Connection>> ();
        for (String e : edges) {
            PageModel src = getEdgeSource(e);
            PageModel tgt = getEdgeTarget(e);
            Widget swi = findWidget(src);
            Widget stg = findWidget(tgt);
            Connection c = new Connection (swi, stg);
            Set <Connection> s = cs.get (swi);
            if (s == null) {
                cs.put (swi, s = new HashSet <Connection> ());
            }
            s.add (c);
            cs.put (swi, s);
            List <Widget> l = m.get (swi);
            if (l == null) {
                l = new ArrayList <Widget> ();
                m.put (swi, l);
            }
            l.add (stg);
        }
        try {
            Utilities.topologicalSort(widgets, m);
        } catch (TopologicalSortException e) {
            e.printStackTrace();
        }
        return new WidgetData (widgets, cs);
    }
    
    
    public static final class WidgetData {
        private final List <Widget> w;
        private final Map <Widget, Set <Connection>> conns;
        WidgetData (List <Widget> w, Map <Widget, Set<Connection>> conns) {
            this.w = w;
            this.conns = conns;
        }
        public List <Widget> getAllWidgets() {
            return w;
        }
        
        public boolean isOrphan() {
            return false;
        }
        
        public boolean isBidi (Connection c) {
            Set <Connection> s = conns.get (c.getTarget());
            for (Connection cc : s) {
                if (cc.isInverse(c)) {
                    return true;
                }
            }
            return false;
        }
        
        public List <Widget> getNextSetOfWidgets (Widget w) {
            List <Widget> l = new ArrayList <Widget> ();
            Set <Connection> c = conns.get (w);
            if (c != null) {
                for (Connection conn : c) {
                    if (conn.getSource() == w) {
                        l.add (conn.getTarget());
                    }
                }
            }
            return l;
        }
        
        public List <Widget> getOrphans() {
            List <Widget> result = new ArrayList <Widget> (w);
            for (Iterator <Widget> i = result.iterator(); i.hasNext();) {
                if (conns.containsKey (i.next())) {
                    i.remove();
                }
            }
            return result;
        }
    }
    
    public class Connection {
        private Widget src;
        private Widget tgt;
        Connection (Widget src, Widget tgt) {
            this.src = src;
            this.tgt = tgt;
        }
        public Widget getSource() {
            return src;
        }
        
        public Widget getTarget() {
            return tgt;
        }
        
        public boolean equals (Object o) {
            boolean result = o instanceof Connection;
            if (result) {
                Connection c = (Connection) o;
                result = c.tgt == tgt && c.src == src;
            }
            return result;
        }
        
        public boolean isInverse (Connection c) {
            boolean result = c.tgt == src && c.src == tgt;
            return result;
        }
        
        public int hashCode() {
            return src.hashCode() * tgt.hashCode();
        }
    }

    protected void attachEdgeSourceAnchor (String edge, PageModel oldSourceNode, PageModel sourceNode) {
        System.err.println("Attach edge source anchor " + edge + " sourceNode " + sourceNode);
        Widget w = sourceNode != null ? findWidget (sourceNode) : null;
        ((ConnectionWidget) findWidget (edge)).setSourceAnchor (AnchorFactory.createRectangularAnchor (w));
    }

    protected void attachEdgeTargetAnchor (String edge, PageModel oldTargetNode, PageModel targetNode) {
        System.err.println("Attach edge target anchor " + edge + " targetNode " + targetNode);
        Widget w = targetNode != null ? findWidget (targetNode) : null;
        ((ConnectionWidget) findWidget (edge)).setTargetAnchor (AnchorFactory.createRectangularAnchor (w));
    }
    
    private class WidgetDragAction extends WidgetAction.Adapter {
        Widget w;
        Point p;
        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            p = event.getPoint();
            w = widget;
            return State.CONSUMED;
        }
        
        public State mouseDragged (Widget widget, WidgetMouseEvent event) {
            Point pt = event.getPoint();
            if (p != null) {
                int offx = pt.x - p.x;
                int offy = pt.y - p.y;
                if (offx == 0 && offy == 0) {
                    return State.REJECTED;
                }
                Point old = widget.getLocation();
                widget.setPreferredLocation(new Point (old.x + offx, old.y + offy));
                ConnectScene.this.validate();
                widget.repaint();
            }
            p = pt;
            return State.CONSUMED;
        }
        
        public State mouseReleased (Widget widget, WidgetMouseEvent event) {
            w = null;
            p = null;
            return State.REJECTED;
        }
        
    }

    private class MultiSelectAction extends WidgetAction.Adapter {
        Point start;
        Point end;
        public boolean isDragging() {
            return start != null || end != null;
        }
        
        public State mousePressed (Widget widget, WidgetMouseEvent event) {
            if (widget != ConnectScene.this) {
                System.err.println("wrong widget: " + widget);
                return State.REJECTED;
            }
            start = event.getPoint();
            setEnd (event.getPoint());
            return State.CONSUMED;
        }
        
        public State mouseDragged (Widget widget, WidgetMouseEvent event) {
            if (widget != ConnectScene.this) {
                System.err.println("wrong widget: " + widget);
                return State.REJECTED;
            }
            setEnd(event.getPoint());
            return State.CONSUMED;
        }
        
        private SelectionWidget selectionWidget;
        private void setEnd (Point p) {
            System.err.println("SetEnd " + p);
            if (p == null || start == null) {
                System.err.println("end null, removing");
                selectionLayer.removeChildren();
                end = null;
                return;
            }
            Point old = end;
            end = p;
            if (old == null) {
                selectionLayer.addChild (selectionWidget = new SelectionWidget(ConnectScene.this));
            }
            System.err.println("start is " + start);
            System.err.println("end is " + end);
            if (start == null) {
                System.err.println("Start null, removing");
                selectionLayer.removeChild (selectionWidget);
                return;
            }
            int x = Math.min (start.x, end.x);
            int y = Math.min (start.y, end.y);
            int w = Math.max (start.x, end.x) - x;
            int h = Math.max (start.y, end.y) - y;
            Rectangle r = new Rectangle (x, y, w, h);
            Point pt = new Point (x, y);
            pt = selectionLayer.convertSceneToLocal(pt);
            r.setLocation (pt);
            selectionWidget.setPreferredSize(r.getSize());
            selectionWidget.setPreferredLocation(pt);
            getScene().validate();
            selectionLayer.repaint();
        }
        
        public State mouseReleased (Widget widget, WidgetMouseEvent event) {
            if (widget != ConnectScene.this) {
                return State.REJECTED;
            }
            if (start == null || end == null) {
                widget.repaint();
                return State.REJECTED;
            }
            setEnd (event.getPoint());
            int x = Math.min (start.x, end.x);
            int y = Math.min (start.y, end.y);
            int w = Math.max (start.x, end.x) - x;
            int h = Math.max (start.y, end.y) - y;
            Point p = widget.convertSceneToLocal(new Point (x, y));
            x = p.x;
            y = p.y;
            Rectangle selectedBounds = new Rectangle (x, y, w, h);
            Set <Object> toSelect = new HashSet <Object> (10);
            for (PageModel nd : ConnectScene.this.getNodes()) {
                Widget test = ConnectScene.this.findWidget(nd);
                Rectangle r = test.convertLocalToScene(test.getBounds());
                Object key = ConnectScene.this.findObject(test);
                boolean select = selectedBounds.contains (r);
                if (!select) {
                    select = r.contains (new Point (x, y));
                }
                if (selectedBounds.contains(r)) {
                    toSelect.add (key);
                }
            }
            setSelectedObjects(toSelect);
            setHighlightedObjects(toSelect);
            ConnectScene.this.repaint();
            start = null;
            setEnd (null);
            return State.CONSUMED;
        }

        public State mouseExited (Widget widget, WidgetMouseEvent event) {
            if (start != null || end != null) {
                setEnd (null);
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
 
        public State mouseClicked (Widget widget, WidgetMouseEvent event) {
            if (event.getClickCount () >= 2)
                if (event.getButton () == MouseEvent.BUTTON1 || event.getButton () == MouseEvent.BUTTON2) {
                    PageModel pg = mdl.createNewPage("New Step" + nodeCounter ++);
                    
                    Widget w = addNode (pg);
                    w.setPreferredLocation(event.getPoint());
                    return State.CONSUMED;
                }
            return State.REJECTED;
        }
    }

    private class SceneConnectProvider implements ConnectProvider {

        private PageModel source = null;
        private PageModel target = null;

        public boolean isSourceWidget (Widget sourceWidget) {
            Object object = findObject (sourceWidget);
            source = isNode (object) ? (PageModel) object : null;
            return source != null;
        }

        public ConnectorState isTargetWidget (Widget sourceWidget, Widget targetWidget) {
            Object object = findObject (targetWidget);
            target = isNode (object) ? (PageModel) object : null;
            if (target != null)
                return ! source.equals (target) ? ConnectorState.ACCEPT : ConnectorState.REJECT_AND_STOP;
            return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
        }

        public boolean hasCustomTargetWidgetResolver (Scene scene) {
            return false;
        }

        public Widget resolveTargetWidget (Scene scene, Point sceneLocation) {
            return null;
        }
        
        private PageModel mdlFor (Widget w) {
            System.err.println("Model FOr " + w);
            if (w instanceof LabelWidget) {
                w = w.getParentWidget();
            }
            if (w instanceof ImageWidget) {
                w = w.getParentWidget();
            }
            return w instanceof PageWidget ? ((PageWidget) w).getPageModel() : null;
        }

        public void createConnection (Widget sourceWidget, Widget targetWidget) {
            if (sourceWidget == targetWidget) {
                return;
            }
            PageModel src = mdlFor (sourceWidget);
            PageModel dest = mdlFor (targetWidget);
            if (src == null || dest == null) {
                return;
            }
            
            String problem = dest.addOrigin(src);
            if (problem == null) {
                String edge = "edge" + edgeCounter ++;
                addEdge (edge);
                setEdgeSource (edge, source);
                setEdgeTarget (edge, target);
            } else {
                setHintText (problem);
            }
            if (problem == null) {
                problem = mdl.getProblem();
                if (problem != null) {
                    setHintText (problem);
                }
            }
            reflow();
        }
    }
    
    private void setHintText(String problem) {
        if (hintWidget.getParentWidget() != selectionLayer) {
            selectionLayer.addChild(hintWidget);
        }
        hintWidget.setText (problem);
    }

    private class SceneReconnectProvider implements ReconnectProvider {

        String edge;
        PageModel originalNode;
        PageModel replacementNode;

        public void reconnectingStarted (ConnectionWidget connectionWidget, boolean reconnectingSource) {
        }

        public void reconnectingFinished (ConnectionWidget connectionWidget, boolean reconnectingSource) {
        }

        public boolean isSourceReconnectable (ConnectionWidget connectionWidget) {
            Object object = findObject (connectionWidget);
            edge = isEdge (object) ? (String) object : null;
            originalNode = edge != null ? getEdgeSource (edge) : null;
            return originalNode != null;
        }

        public boolean isTargetReconnectable (ConnectionWidget connectionWidget) {
            Object object = findObject (connectionWidget);
            edge = isEdge (object) ? (String) object : null;
            originalNode = edge != null ? getEdgeTarget (edge) : null;
            return originalNode != null;
        }

        public ConnectorState isReplacementWidget (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource) {
            Object object = findObject (replacementWidget);
            replacementNode = isNode (object) ? (PageModel) object : null;
            if (replacementNode != null)
                return ConnectorState.ACCEPT;
            return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
        }

        public boolean hasCustomReplacementWidgetResolver (Scene scene) {
            return false;
        }

        public Widget resolveReplacementWidget (Scene scene, Point sceneLocation) {
            return null;
        }
        
        public void reconnect (ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource) {
            if (replacementWidget == null) {
                PageModel tgt = getEdgeTarget (edge);
                PageModel src = getEdgeSource(edge);
                tgt.removeOrigin(src);
                src.removeDestination(tgt);
                System.err.println("Disconnected " + tgt + " from " + src);
                removeEdge (edge);
            } else if (reconnectingSource) {
                setEdgeSource (edge, replacementNode);
            } else {
                setEdgeTarget (edge, replacementNode);
            }
        }
    }
    
    private final class SelectionWidget extends Widget {
        public SelectionWidget (Scene s) {
            super (s);
        }
        
        protected void paintWidget () {
            Graphics2D g = getScene().getGraphics();
            Composite old = g.getComposite();
            g.setComposite (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                    0.2f));
            Rectangle r = getBounds();
            g.setColor (Color.BLUE);
            g.fillRect (0, 0, r.width, r.height);
            g.setColor (Color.BLACK);
            g.drawRect (0, 0, r.width, r.height);
            g.setComposite (old);
        }        
    }

    private class DeleteAction extends WidgetAction.Adapter {
        public State keyReleased(Widget widget, WidgetKeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_DELETE) {
                PageModel page = (PageModel) findObject(widget);
                mdl.delete(page);
                removeNode(page);
                return State.CONSUMED;
            }
            return State.REJECTED;
        }
    }
    
    private class WrapperAction implements WidgetAction {
        WidgetAction other;
        WrapperAction (WidgetAction other) {
            this.other = other;
        }
        
        private boolean shouldReact (WidgetMouseEvent evt) {
            return evt.isShiftDown();
        }
    
        public State mouseClicked(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mouseClicked(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mousePressed(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mouseReleased(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mouseReleased(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mouseEntered(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mouseEntered(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mouseExited(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mouseExited(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mouseDragged(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mouseDragged(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mouseMoved(Widget widget, WidgetMouseEvent event) {
            boolean result = shouldReact (event);
            if (result) {
                return other.mouseMoved(widget, event);
            }
            return result ? State.CONSUMED : State.REJECTED;
        }

        public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {
            return State.REJECTED;
        }

        public State keyTyped(Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }

        public State keyPressed(Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }

        public State keyReleased(Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }

        public State focusGained(Widget widget, WidgetFocusEvent event) {
            return State.REJECTED;
        }

        public State focusLost(Widget widget, WidgetFocusEvent event) {
            return State.REJECTED;
        }

        public State dragEnter(Widget widget, WidgetDropTargetDragEvent event) {
            return State.REJECTED;
        }

        public State dragOver(Widget widget, WidgetDropTargetDragEvent event) {
            return State.REJECTED;
        }

        public State dropActionChanged(Widget widget,
                                       WidgetDropTargetDragEvent event) {
            return State.REJECTED;
        }

        public State dragExit(Widget widget, WidgetDropTargetEvent event) {
            return State.REJECTED;
        }

        public State drop(Widget widget, WidgetDropTargetDropEvent event) {
            return State.REJECTED;
        }
    }
    
    private class TextFieldInplaceEditorImpl implements TextFieldInplaceEditor {
        public boolean isEnabled(Widget widget) {
            return true;
        }

        public String getText(Widget widget) {
            PageWidget w = (PageWidget) widget;
            return w.getPageModel().getDescription();
        }

        public void setText(Widget widget, String text) {
            PageWidget w = (PageWidget) widget;
            w.getPageModel().setDescription (text);
            w.setToolTipText(text);
            w.setLabel (PageWidget.trim(text));
        }
    }
}
