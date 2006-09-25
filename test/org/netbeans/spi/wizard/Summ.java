/*
 * Summ.java
 *
 * Created on September 24, 2006, 4:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.spi.wizard;

import java.awt.Component;

/**
 * Subclass of summary for tests
 *
 * @author Tim Boudreau
 */
public class Summ extends Summary {
    public Summ (String s, Object result) {
        super (s, result);
    }
    public Summ (String[] s, Object result) {
        super (s, result);
    }
    public Summ (Component s, Object result) {
        super (s, result);
    }

    public Component getSummaryComponent() {
        Component retValue;
        s = true;
        retValue = super.getSummaryComponent();
        return retValue;
    }

    private boolean s;
    private boolean r;
    public Object getResult() {
        Object retValue;
        r = true;
        retValue = super.getResult();
        return retValue;
    }
    
    public boolean summaryComponentWasCalled() {
        boolean result = s;
        s = false;
        return result;
    }
    
    public boolean getResultWasCalled() {
        boolean result = r;
        r = false;
        return result;
    }
}
