/*  The contents of this file are subject to the terms of the Common Development
 and Distribution License (the License). You may not use this file except in
 compliance with the License.
 You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 or http://www.netbeans.org/cddl.txt.
 When distributing Covered Code, include this CDDL Header Notice in each file
 and include the License file at http://www.netbeans.org/cddl.txt.
 If applicable, add the following below the CDDL Header, with the fields
 enclosed by brackets [] replaced by your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]" */

package loginWizardDemo;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
 * Demostrate the wizard use of background operations (DeferredWizardResult)
 * as part of the processing when user presses the "next" button.
 * 
 * @author stanley@stanleyknutson.com
 */
public class LoginWizardDemoMain
{
    public static void main(String[] ignored) throws Exception
    {
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
        LoginWizardDemoMain main = new LoginWizardDemoMain ();
        main.run ();
    }
    
    private void run ()
    {
        try
        {
            runWizard();
        }
        finally
        {
            // make sure the database gets closed
            DbLoginPanel.ensureConnectionClosed();
            System.exit(0);
        }
    }
    
    private void runWizard()
    {
        // All we do here is assemble the list of WizardPage subclasses we
        // want to show:
        Class[] pages = new Class[] {DbLoginPanel.class, TablePickerPanel.class,};

        // Use the utility method to compose a Wizard
        Wizard wizard = WizardPage.createWizard(pages, new ComputeResult());

        // preload the previous values
        Map previousValues = getPreviousValues();

        Rectangle size = new Rectangle(0, 0, 600, 500);
        Object results = WizardDisplayer.showWizard(wizard, size, null, previousValues);

        // saveValues(); // save the input values for next use

        System.out.println("Wizard results " + results);
    }

    Map getPreviousValues()
    {
        Properties prop = new Properties();

        // a full implementation reads them from a properties file

        prop.setProperty(LoginConstants.HOSTNAME_PROPERTY, "QADB");
        prop.setProperty(LoginConstants.PORT_PROPERTY, "1521");
        prop.setProperty(LoginConstants.SID_PROPERTY, "qadb");
        prop.setProperty(LoginConstants.USERNAME_PROPERTY, "sk_def");

        return prop;
    }

    private static class ComputeResult implements WizardResultProducer
    {
        public Object finish(Map wizardData) throws WizardException
        {
            // We will just return the wizard data here. In real life we would
            // a compute a result here
            Summary summary;
            Object[][] data = new Object[wizardData.size()][2];
            int ix = 0;
            for (Iterator i = wizardData.keySet().iterator(); i.hasNext();)
            {
                Object key = i.next();
                Object val = wizardData.get(key);
                data[ix][0] = key;
                data[ix][1] = val;
                ix++;
            }
            TableModel mdl = new DefaultTableModel(data, new String[] {"Key", "Value"});
            summary = Summary.create(new JScrollPane(new JTable(mdl)), wizardData);
            return summary;
        }

        public boolean cancel(Map settings)
        {
            // not really supported
            return true;
        }
    }

}
