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
package net.java.dev.wizard.template.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
public class WizardModel {
    private Set <String> uids = new HashSet <String> ();
    private Map <String, PageModel> pages = new HashMap <String, PageModel> ();
    
    /** Creates a new instance of WizardModel */
    public WizardModel(String... pages) {
        String[] ids = new String[pages.length];
        PageModel prev = null;
        for (int i = 0; i < ids.length; i++) {
            ids[i] = createUid(pages[i]);
            PageModel p = new PageModel (ids[i], pages[i], prev);
            this.pages.put (ids[i], p);
            prev = p;
        }
        uids = new HashSet <String> (Arrays.<String>asList(ids));
        if (uids.size() != pages.length) {
            throw new IllegalArgumentException ("Duplicate ids: " + Arrays.asList(pages));
        }
    }
    
    public List <PageModel> getOrphans() {
        List <PageModel> result = new LinkedList <PageModel> ();
        for (Iterator <PageModel> i = result.iterator(); i.hasNext();) {
            if (!i.next().isOrphan()) i.remove();
        }
        return result;
    }
    
    public PageModel createNewPage (String txt) {
        String id = createUid (txt);
        PageModel result = new PageModel (id, txt, null);
        uids.add (id);
        this.pages.put (id, result);
        return result;
    }
    
    public void delete (PageModel mdl) {
        uids.remove (mdl.getUID());
        pages.remove (mdl.getUID());
        for (String uid : uids) {
            PageModel p = pages.get (uid);
            p.removeDestination(mdl);
            p.removeOrigin(mdl);
        }
    }
    
    public PageModel getRootPage() {
        PageModel result = null;
        for (String uid : uids) {
            result = pages.get (uid);
            if (result.isStart() && !result.isEnd()) {
                break;
            }
        }
        return result;
    }
    
    public Collection <PageModel> getPages() {
        return pages.values();
    }
        
    public String getProblem() {
        boolean hasOrphans = false;
        int starts = 0;
        for (PageModel p : getPages()) {
            hasOrphans |= p.isOrphan();
            if (p.isStart() && !p.isOrphan()) {
                starts++;
            }
        }
        if (starts == 0) {
            return "There must be a first page that nothing links to";
        } else if (starts >= 1) {
//            return "There must be only one start page";
            return null;
        }
        if (hasOrphans) {
            return "Orphan, unlinked pages present";
        }
        return null;
    }
    
    private String createUid (String pg) {
        pg = pg.toLowerCase();
        if (pg.length() > 10) {
            pg = pg.substring(0, 10);
        }
        pg = pg.replace(' ', '_');
        if (uids.contains(pg)) {
            int ix = 0;
            String s = pg + "_" + ix;
            while (uids.contains (s)) {
                s = pg + "_" + (ix++);
            }
            pg = s;
        }
        return pg;
    }
    
    public String toString() {
        return pages.toString();
    }
    
}
