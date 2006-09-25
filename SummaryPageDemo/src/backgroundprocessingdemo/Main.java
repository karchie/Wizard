/*
 * Main.java
 *
 * Created on September 24, 2006, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package backgroundprocessingdemo;

import java.util.Iterator;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

/**
 *
 * @author Tim Boudreau
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Wizard wiz = 
                WizardPage.createWizard(new Class[] { 
                FirstPage.class, SecondPage.class }, 
                new WRP());
        
        System.out.println("Wizard Result: " + WizardDisplayer.showWizard(wiz));
    }
    
    private static class WRP implements WizardResultProducer {
        public Object finish(Map wizardData) throws WizardException {
            //We will just return the wizard data here.  In real life we would
            //a compute a result here
            Summary summary;
            if (Boolean.TRUE.equals(wizardData.get("custom"))) {
                Object[][] data = new Object[wizardData.size()][2];
                int ix = 0;
                for (Iterator i=wizardData.keySet().iterator(); i.hasNext();) {
                    Object key = i.next();
                    Object val = wizardData.get(key);
                    data[ix][0] = key;
                    data[ix][1] = val;
                    ix++;
                }
                TableModel mdl = new DefaultTableModel (data, 
                        new String[] { "Key", "Value"});
                summary = Summary.create(new JScrollPane(new JTable(mdl))
                        , wizardData);
            } else if (Boolean.TRUE.equals(wizardData.get("list"))) {
                String[] s = new String[wizardData.size()];
                int ix = 0;
                for (Iterator i=wizardData.keySet().iterator(); i.hasNext();) {
                    Object key = i.next();
                    s[ix] = key + " = " + wizardData.get(key);
                    ix++;
                }
                summary = Summary.create (s, wizardData);
            } else if (Boolean.TRUE.equals(wizardData.get("plainText"))) {
                summary = Summary.create ("This is some summary text", wizardData);
            } else {
                summary = null;
            }
            return summary == null ? wizardData : summary;
        }

        public boolean cancel(Map settings) {
            System.err.println("CANCELLED: " + settings);
            return true;
        }
    }
    
}
