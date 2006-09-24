/*
 * NbBridge.java
 *
 * Created on September 23, 2006, 6:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wizard;

import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.api.wizard.WizardDisplayer;

/**
 * Non API class for accessing a few things in NetBeans via reflection.
 *
 * @author Tim Boudreau
 */
public final class NbBridge {
    private NbBridge() {}
    static Boolean inNetBeans = null;
    public static boolean inNetBeans() {
        if (inNetBeans == null) {
            try {
                Class clazz = Class.forName("org.openide.util.Lookup"); //NOI18N
                clazz = Class.forName("org.openide.util.NbBundle"); //NOI18N
                inNetBeans = Boolean.TRUE;
            } catch (Exception e) {
                inNetBeans = Boolean.FALSE;
            }
        }
        return inNetBeans.booleanValue();
    }

    private static Method lkpMethod;
    public static WizardDisplayer getFactoryViaLookup() {
        if (inNetBeans()) {
            try {
                if (lkpMethod == null) {
                    Class clazz = Class.forName("org.openide.util.Lookup"); //NOI18N
                    lkpMethod = clazz.getMethod("lookup", Class.class); //NOI18N
                }
                return (WizardDisplayer)
                        lkpMethod.invoke(null, WizardDisplayer.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Method bundleMethod;
    private static String getStringViaNbBundle(Class clazz, String key) {
        if (inNetBeans()) {
            try {
                if (bundleMethod == null) {
                    Class c = Class.forName("org.openide.util.NbBundle"); //NOI18N
                    bundleMethod = c.getMethod("getMessage", Class.class, String.class); //NOI18N
                }
                return (String) bundleMethod.invoke (null, new Object[] {
                    clazz, key
                });
            } catch (MissingResourceException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getString (String path, Class callerType, String key) {
        String result = getStringViaNbBundle (callerType, key);
        if (result == null) {
            result = getStringViaResourceBundle (path, key);
        }
        return result;
    }

    private static String getStringViaResourceBundle (String path, String key) {
        return ResourceBundle.getBundle(path).getString(key);
    }
}
