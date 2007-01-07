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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
public class PageModel {
    private final String id;
    private String description;
    /** Creates a new instance of PageModel */
    public PageModel(String id, String description, PageModel prev) {
        this.id = id;
        this.description = description;
        assert description != null;
        assert id != null;
        if (prev != null) {
            origins.add (prev);
            prev.destinations.add (this);
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUID() {
        return id;
    }
    
    public boolean isBranchPoint() {
        return destinations.size() > 1;
    }
    
    public boolean isEnd() {
        return destinations.isEmpty();
    }
    
    public boolean isStart() {
        return origins.isEmpty();
    }
    
    public boolean isOrphan() {
        return isEnd() && isStart();
    }
    
    public void setDescription (String description) {
        if (description != null && description.trim().length() == 0) {
            description = null;
        }
        description = description;
    }
    
    private String longDescription = null;
    public void setLongDescription (String description) {
        if (description != null && description.trim().length() == 0) {
            description = null;
        }
        longDescription = description;
    }
    
    public String getLongDescription() {
        return longDescription;
    }
    
    public boolean removeOrigin (PageModel mdl) {
        return origins.remove(mdl);
    }
    
    public boolean removeDestination (PageModel mdl) {
        return destinations.remove (mdl);
    }
    
    public String toString() {
        return '"' + getDescription() + '"';
    }
    
    private List <PageModel> destinations = new ArrayList <PageModel> ();
    private List <PageModel> origins = new ArrayList <PageModel> ();
    private boolean inAddDestination = false;
    public String addDestination (PageModel pg) {
        if (inAddDestination) {
            return null;
        }
        inAddDestination = true;
        try {
            String result = checkValidDestination (pg, true);
            if (result == null) {
                result = pg.addOrigin(this);
                if (result == null) {
                    destinations.add (pg);
                }
            }
            return result;
        } finally {
            inAddDestination = false;
        }
    }
    
    public List <PageModel> getDestinations() {
        return new ArrayList <PageModel> (destinations);
    }
    
    public List <PageModel> getOrigins() {
        return new ArrayList <PageModel> (origins);
    }
    
    boolean inAddOrigin = false;
    public String addOrigin (PageModel origin) {
        if (inAddOrigin) {
            return null;
        }
        inAddOrigin = true;
        try {
            String result = checkValidOrigin (origin, true);
            if (result == null) {
                if (result == null) {
                    result = origin.addDestination(this);
                    if (result == null) {
                        origins.add (origin);
                    }
                }
            }
            return result;
        } finally {
            inAddOrigin = false;
        }
    }
    
    String checkValidDestination (PageModel pg, boolean recurse) {
        if (pg == this) {
            return "Cannot connect a page to itself";
        }
        if (destinations.contains (pg)) {
            return "Already have a reference to " + pg;
        }
        if (recurse) {
            return rCheckDestination (pg, new HashSet());
        }
        return null;
    }
    
    String checkValidOrigin (PageModel pg, boolean recurse) {
        if (pg == this) {
            return "Cannot connect a page to itself";
        }
        if (origins.contains(pg)) {
            return "Already connected to " + pg;
        }
        if (recurse) {
            return rCheckOrigin (pg, new HashSet());
        }
        return null;
    }
        
    private String rCheckDestination(PageModel pg, Set seen) {
        List <PageModel> l = pg.getDestinations();
        for (PageModel p : l) {
            if (seen.contains (p)) {
                continue;
            }
            if (p == this) {
                return "Circular connections not allowed";
            } else {
                String result = p.checkValidDestination(this, false);
                if (result != null) {
                    return result;
                }
            }
            seen.add (p);
        }
        return null;
    }
    
    private String rCheckOrigin(PageModel pg, Set seen) {
        List <PageModel> l = pg.getOrigins();
        for (PageModel p : l) {
            if (seen.contains (p)) {
                continue;
            }
            if (p == this) {
                return "Circular connections not allowed";
            } else {
                String result = p.checkValidOrigin(this, false);
                if (result != null) {
                    return result;
                }
            }
            seen.add (p);
        }
        return null;
    }
    
}
