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
package net.java.dev.wizard.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tim Boudreau
 */
public class Pages {
    private final Map <String, PageDescriptor> contents = new HashMap <String, PageDescriptor> ();
    public void put (String id, PageDescriptor descriptor) {
        contents.put (id, descriptor);
    }
    
    private final List <String> ids = new ArrayList <String> ();
    public boolean add (String id) {
        boolean result = !contents.containsKey(id);
        if (!result) {
            ids.add (id);
        }
        return result;
    }
    
    public List <String> allIds() {
        return Collections.<String>unmodifiableList (ids);
    }
    
    public List <String> missingIds() {
        List <String> l = new ArrayList <String> (ids);
        l.removeAll (contents.keySet());
        return l;
    }
    
    public boolean isComplete() {
        return contents.keySet().containsAll (ids);
    }
    
    public String getNextMissingId() {
        for (String id : ids) {
            if (!contents.containsKey (id)) {
                return id;
            }
        }
        return null;
    }
}
