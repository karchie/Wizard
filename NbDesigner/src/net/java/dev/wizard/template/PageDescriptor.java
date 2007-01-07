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

/**
 *
 * @author Tim Boudreau
 */
public class PageDescriptor {
    private int branchCount = 0;
    private String shortDescription;
    public PageDescriptor() {
    }
    
    public void setShortDescription(String sd) {
        shortDescription = sd;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    private String longDescription;
    public String getLongDescription() {
        if (longDescription != null) {
            return "".equals(longDescription.trim()) ? null : longDescription;
        }
        return null;
    }
    
    public void setLongDescription (String s) {
        longDescription = s;
    }
    
    public void setBranchCount (int ct) {
        branchCount = ct;
    }
    
    private String title;
    public String getTitle() {
        return title;
    }
    
    public void setTitle (String s) {
        this.title = s;
    }
    
    public int getBranchCound() {
        return branchCount;
    }
    
    public String[] toJavaSource() {
        return null;
    }
}
