/*
 * AdHocContainer.java
 *
 * Created on September 22, 2006, 12:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.spi.wizard;

import java.awt.FlowLayout;
import javax.swing.JComponent;

/**
 *
 * @author Tim Boudreau
 */
public class AdHocContainer extends JComponent {
    public AdHocContainer() {
        setLayout (new FlowLayout());
    }
}
