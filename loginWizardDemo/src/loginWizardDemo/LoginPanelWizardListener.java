package loginWizardDemo;

import java.awt.Component;

import javax.swing.text.JTextComponent;

import org.netbeans.spi.wizard.GenericListener;
import org.netbeans.spi.wizard.WizardPage;

/**
 * Listen for proper components within the various utility classes that 
 * are used within the wizard.
 * 
 * @author stanley@stanleyknutson.com
 */
// TODO: there should be a better way to do this in the generic listener
public class LoginPanelWizardListener extends GenericListener
{

    public LoginPanelWizardListener(WizardPage page)
    {
        super(page);
    }

    protected void attachTo(Component jc)
    {
        Component inner = getInnerComponent(jc);
        if (inner != null)
        {
            super.attachTo(inner);
        }
        else
        {
            super.attachTo(jc);
        }
    }

    protected void detachFrom(Component jc)
    {
        Component inner = getInnerComponent(jc);
        if (inner != null)
        {
            super.detachFrom(inner);
        }
        else
        {
            super.detachFrom(jc);
        }
    }
    
    /**
     * Override to handle our various subclasses of JTextArea and JTextField
     * that turn the content red if the input is not valid.
     *
     * @see org.netbeans.spi.wizard.GenericListener#isProbablyAContainer(java.awt.Component)
     */
    protected boolean isProbablyAContainer(Component c)
    {
        if (c instanceof JTextComponent)
        {
            return false;
        }
        return super.isProbablyAContainer(c);
    }

    /**
     * Return the component to be used for listening.
     * In particular, handle the LabeledComponent which has a prompt above
     * a input field (usually a subclass of JTextField created by ComponentFactory)
     * 
     * @param jc
     * @return inner component that is the real user input, or null if there is none.
     */
    protected Component getInnerComponent (Component jc)
    {
        if (jc instanceof LabeledComponent)
        {
            Component inner = ((LabeledComponent)jc).getCenterComponent();
            return inner;
        }
        return null;
    }

}

