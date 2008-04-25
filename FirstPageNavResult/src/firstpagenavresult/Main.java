/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package firstpagenavresult;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;
import javax.swing.JCheckBox;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
import org.netbeans.spi.wizard.WizardPanel;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author tim
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Wizard w = WizardPage.createWizard(new Class[] { A.class, B1.class, C1.class, D1.class},
                new WizardResultProducer() {

            public Object finish(Map wizardData) throws WizardException {
                DeferredWizardResult res = new DeferredWizardResult() {

                    @Override
                    public void start(Map settings, ResultProgressHandle progress) {
                        try {
                            progress.setProgress(1, 4);
                            Thread.sleep(500);
                            progress.setProgress(2, 4);
                            Thread.sleep(500);
                            progress.setProgress(3, 4);
                            Thread.sleep(500);
                            progress.finished(Summary.create("Hi there", null));
//                            progress.finished(null);
                        } catch (Exception e) {
                            
                        }
                    }
                    
                };
                return res;
            }

            public boolean cancel(Map settings) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
        });
        WizardDisplayer.showWizard(w);
    }
    
    private static class Base extends WizardPage implements WizardPanel {
        final JCheckBox box;
        public Base (String id) {
            super (id, id, true);
            setLayout (new BorderLayout());
            box = new JCheckBox(id);
            box.setName(id);
            add(box, BorderLayout.CENTER);
        }

        @Override
        protected String validateContents(Component component, Object event) {
            if (box.isSelected()) {
                return null;
            }
            return box.getText() + " not checked";
        }

        @Override
        public WizardPanelNavResult allowNext(String stepName, Map settings, Wizard wizard) {
            if ("A".equals(box.getText())) {
                return new WizardPanelNavResult() {

                    @Override
                    public boolean isDeferredComputation() {
                        return true;
                    }

                    @Override
                    public void start(Map settings, ResultProgressHandle progress) {
                        try {
                            progress.setProgress(1, 4);
                            Thread.sleep(500);
                            progress.setProgress(2, 4);
                            Thread.sleep(500);
                            progress.setProgress(3, 4);
                            Thread.sleep(500);
                            progress.finished(null);
                        } catch (Exception e) {
                            
                        }
                    }
                    
                };
            }
            return super.allowNext(stepName, settings, wizard);
        }
        
        
    }
    
    public static class A extends Base {
       public A() {
           super ("A");
       } 
       
       public static String getDescription() {
           return "A";
       }
    }
    
    public static class B1 extends Base {
       public B1() {
           super ("B1");
       } 
       public static String getDescription() {
           return "B1";
       }
    }
    
    public static class C1 extends Base {
       public C1() {
           super ("C1");
       } 
       public static String getDescription() {
           return "C1";
       }
    }
    
    public static class D1 extends Base {
       public D1() {
           super ("D1");
       } 
       public static String getDescription() {
           return "D1";
       }
    }
    
    public static class B2 extends Base {
       public B2() {
           super ("B2");
       } 
    }
    
    public static class C2 extends Base {
       public C2() {
           super ("C2");
       } 
    }
    
    public static class D2 extends Base {
       public D2() {
           super ("D2");
       } 
    }
}
