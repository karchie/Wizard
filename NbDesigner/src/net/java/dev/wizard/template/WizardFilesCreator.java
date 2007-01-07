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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.java.dev.wizard.template.model.PageModel;
import net.java.dev.wizard.template.model.WizardModel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wizard2.WizardFactory;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tim Boudreau
 */
public class WizardFilesCreator extends DeferredWizardResult {
    
    public void start(Map m, ResultProgressHandle h) {
        WizardModel mdl = (WizardModel) m.get(BranchDesignerPanel.KEY_PAGES);
        Collection <PageModel> pages = mdl.getPages();
        String title = (String) m.get ("title");
        if (h != null) {
            h.setProgress("Creating files...", 1, pages.size() + 1);
        }
        Map <PageModel, JFile> jfiles = new HashMap <PageModel, JFile> ();
        Set <String> usedClassNames = new HashSet <String> ();
        int ix = 1;
        PFile commonBundle = new PFile("Bundle", "properties");
        Set <FileModel> files = new HashSet <FileModel> ();
        files.add (commonBundle);
        
        FileObject targetFolder = (FileObject) m.get (WizardFactory.KEY_TARGET_FOLDER);
        if (targetFolder == null) {
            //XXX add input area on first page
            throw new NullPointerException ("Null target folder");
        }
        
        String pAckage = packageFor (targetFolder);
        String pathNameSlashes = pAckage.replace (".", "/");
        
        List <String> pageClassNames = new ArrayList <String> (); //XXX should be map of lists to branches
        for (PageModel p : pages) {
            System.err.println("Create pagemodel for " + p);
            if (h != null) {
                h.setProgress(ix++, pages.size() + 1);
            }
            String className = createClassNameFor(p, usedClassNames) + "Page";
            pageClassNames.add (className);
            System.err.println("ClassName " + className);
            JFile jf = new JFile (className, "java", pAckage);
            files.add (jf);
            jf.setComment ("This is a Swing JPanel subclass.  You can add standard\n" +
                    "Swing components to it.  If you set the Name property of a component\n" +
                    "before you add it, it will automatically be listened to for changes,\n" +
                    "and its value will be put into the map of gathered data automatically.\n");
            jf.addMethod ("validateContents", "String", "//return a problem " +
                    "string if user\n//entered data is wrong or incomplete\n" +
                    "return null; //everything ok\n", 
                    Modifier.PUBLIC, "Component comp", "Object event");
            jf.addMethod("pageRendered", "void", "//Called when this page is navigated to, \n" +
                    "//forwards or backwards. Override, e.g. if you have a \n" +
                    "//JTextField on this page with its Name property set to \"name\",\n" +
                    "//String name = getWizardData (\"name\");\n" +
                    "//if (name != null) nameField.setText(name);\n", Modifier.PUBLIC,"");
            jf.setExtends ("WizardPage");
            jf.addImport("org.netbeans.spi.wizard.WizardPage");
            jf.addImport("java.awt.Component");
            jf.addMethod ("getStep", "String", "return \"" + p.getUID() +"\";",
                    Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, "");
            jf.addImport ("java.util.ResourceBundle");
            String dkey = commonBundle.add (className + ".description");
            String descString = "ResourceBundle.getBundle(\n    \"" + pathNameSlashes + "/Bundle.properties\").getString(\n    \"" + dkey + "\");";
                    
            jf.addMethod ("getDescription", "String", "return " + descString,
                    Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, "");
            if (p.getLongDescription() != null) {
                String key = commonBundle.add (className + ".long.description", p.getLongDescription());
                String bundleCall = "ResourceBundle.getBundle (\n    \"" +
                        pathNameSlashes + "\").getString(\n    \"" + key + "\")";
                jf.addMethod (className, "", "setLongDescription (" + bundleCall + 
                        ");\n", Modifier.PUBLIC, "");
            }
            try {
                jf.write(System.err);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String wizardClass = createClassNameFor(title + " Wizard", usedClassNames);
        JFile wizard = new JFile (wizardClass, "java", pAckage);
        wizard.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
        files.add (wizard);
        wizard.addImplements("WizardResultProducer");
        wizard.addImport("org.netbeans.spi.wizard.WizardPage");
        wizard.addImport("org.netbeans.spi.wizard.WizardPage.WizardResultProducer");
        wizard.addImport("org.netbeans.spi.wizard.Wizard");
        wizard.addImport("org.netbeans.api.wizard.WizardDisplayer");
        String body;
        StringBuilder sb = new StringBuilder(wizardClass + " wiz = new " + wizardClass + "();\n");
        sb.append("return WizardPage.createWizard( new Class[] {\n");
        
        for (String s : pageClassNames) {
            sb.append ("    " + s + ".class,\n");
        }
        sb.append ("}, wiz);\n");
        body = sb.toString();
        wizard.addMethod (wizardClass, "", "", Modifier.PRIVATE, "");
        wizard.addMethod("createWizard", "Wizard", body, Modifier.STATIC, "");
        
        wizard.addMethod("show", "Object", "return WizardDisplayer.showWizard (createWizard());\n", 
                Modifier.PUBLIC | Modifier.STATIC, "");
        
        wizard.addMethod("finish", "Object", "//This method is passed a Map " +
                "containing\n//all of the values gathered as the user traversed " +
                "the panels\n//of this wizard.\n//Compute some object with the\n" +
                "//passed map here.  If you need to display a \n" +
                "//progress bar, implement DeferredWizardResult and return that.\n" +
                "//If you want to display a summary screen, return an instance\n" +
                "//of Summary either here or from your DeferredWizardResult\n" +
                "return null;", Modifier.PUBLIC, "Map gatheredData");
        
        wizard.addMethod("cancel", "boolean", "//This method is called if the user tries to close\n" +
                "//the wizard without finishing it\n" +
                "//Return true to allow the wizard to close.  Perform any\n" +
                "//cleanup you need to here\n" +
                "return true;\n", Modifier.PUBLIC, "Map gatheredData");
        
        wizard.addImport ("java.util.Map");
        if (commonBundle.isEmpty()) {
            files.remove (commonBundle);
        }
        try {
            commonBundle.write(System.err);
            wizard.write (System.err);
            
            for (FileModel f : files) {
                FileObject target = targetFolder.createData(f.getNameExt());
                FileLock lock = target.lock();
                OutputStream out = target.getOutputStream(lock);
                try {
                    f.write (out);
                } finally {
                    out.close();
                    lock.releaseLock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (h != null) {
            h.finished(Collections.EMPTY_SET);
        }
    }
    
    private static String createClassNameFor (PageModel p, Set <String> used) {
        String s = p.getDescription();
        return createClassNameFor (s, used);
    }
    
    private static String packageFor (FileObject fld) {
        ClassPath p = ClassPath.getClassPath(fld, ClassPath.SOURCE);
        if (p == null) {
            return fld.getPath().replace ("/", "."); //XXX
        }
        return p.getResourceName(fld, '.', false);
    }
    
    private static String createClassNameFor (String s, Set <String> used) {    
        char[] c = s.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean lastWasSpace = false;
        for (int i = 0; i < c.length; i++) {
            if (Character.isLetter(c[i])) {
                if (sb.length() == 0 || lastWasSpace) {
                    c[i] = Character.toUpperCase(c[i]);
                }
                sb.append (c[i]);
            } else if (c[i] != ' ') {
                continue;
            }
            lastWasSpace = c[i] == ' ';
            if (lastWasSpace && sb.length() > 10 && !used.contains(sb.toString())) {
                break;
            }
        }
        if (used.contains (sb.toString())) {
            sb.append ("1");
            int ix = 1;
            while (used.contains(sb.toString()) && ix < 10) {
                sb.setCharAt(sb.length() -1, Integer.toString(ix++).charAt(0));
            }
            if (used.contains (sb.toString())) {
                sb.delete(0, sb.length() - 1);
                String str = "Page";
                ix = 1;
                while (used.contains (str + ix++));
                sb.append (str + ix);
            }
        }
        String result = sb.toString();
        used.add (result);
        return result;
    }
    
    private abstract static class FileModel {
        final String name;
        final String ext;
        FileModel (String name, String ext) {
            this.name = name;
            this.ext = ext;
        }
        
        public String getNameExt() {
            return name + "." + ext;
        }
        
        public abstract void write(OutputStream os) throws IOException;
        
        public String toString() {
            return name + "." + ext;
        }
    }
    
    private static final class JFile extends FileModel {
        private final String pAckage;
        public JFile (String name, String ext, String pAckage) {
            super (name, ext);
            this.pAckage = pAckage;
        }
        
        private String comment;
        public void setComment (String comment) {
            assert this.comment == null;
            this.comment = comment;
        }
        
        private String eXtends;
        public void setExtends (String eXtends) {
            this.eXtends = eXtends;
        }
        
        private List <String> impls = new ArrayList <String> ();
        public void addImplements (String impl) {
            impls.add (impl);
        }
        
        private int mods = Modifier.FINAL;
        public void setModifiers (int mods) {
            this.mods = mods;
        }
        
        private List <String> imports = new ArrayList <String> ();
        public void addImport (String iMport) {
            imports.add (iMport);
        }
        
        private List <Method> methods = new ArrayList <Method> ();
        public void addMethod (String name, String returnType, String body, int modifiers, String... args) {
            methods.add (new Method (name, returnType, body, modifiers, args));
        }
    
        public void write(OutputStream os) throws IOException {
            PrintWriter w = new PrintWriter (os);
            w.println ("package " + pAckage + ";");
            Collections.sort (imports);
            for (String s : imports) {
                w.println ("import " + s + ";");
            }
            w.println();
            String extsClause = eXtends == null ? " " : " extends " + eXtends;
            String implsClause;
            Collections.sort (impls);
            if (impls.isEmpty()) {
                implsClause = "";
            } else {
                StringBuilder sb = new StringBuilder(" implements ");
                for (Iterator <String> i = impls.iterator(); i.hasNext();) {
                    sb.append (i.next());
                    if (i.hasNext()) {
                        sb.append (", ");
                    }
                }
                implsClause = sb.toString();
            }
            if (comment != null) {
                String[] cmts = comment.split ("\n");
                for (int i = 0; i < cmts.length; i++) {
                    String string = cmts[i];
                    if (i == 0) {
                        w.println ("/** " + cmts[i]);
                    } else {
                        w.println(" * " + cmts[i]);
                    }
                }
                w.println (" * @author " + System.getProperty("user.name"));
                w.println (" */");
            }
            w.println (Modifier.toString (mods) + " class " + this.name + extsClause + implsClause + " {");
            Collections.sort (methods);
            for (Method m : methods) {
                w.println (m.toString());
            }
            w.println ("}");
            w.close();
        }
        
        private static final class Method implements Comparable {
            final String name;
            final String returnType;
            final String body;
            final String[] args;
            final int modifiers;
            public Method (String name, String returnType, 
                    String body, int modifiers, String[] args) {
                this.name = name;
                this.returnType = returnType;
                this.args = args;
                this.body = body;
                this.modifiers = modifiers;
            }
            
            public String toString() {
                StringBuilder sb = new StringBuilder ("    " + Modifier.toString(modifiers));
                sb.append (" ");
                if (returnType != null && returnType.trim().length() > 0) {
                    sb.append (returnType);
                    sb.append (" ");
                }
                sb.append (name);
                sb.append (" (");
                for (int i=0; i < args.length; i++) {
                    sb.append (args[i]);
                    if (i != args.length - 1) {
                        sb.append (", ");
                    }
                }
                sb.append (") {\n");
                String[] bd = body.split ("\n");
                for (int i = 0; i < bd.length; i++) {
                    sb.append ("        ");
                    sb.append (bd[i]);
                    if (i != bd.length - 1) {
                        sb.append ("\n");
                    }
                }
                sb.append ("\n    }\n");
                return sb.toString();
            }
        
            public int compareTo(Object o) {
                Method m = (Method) o;
                return m.returnType.compareToIgnoreCase(returnType == null ? "" : returnType);
            }
        }
    }
    
    private static final class PFile extends FileModel {
        public PFile (String name, String ext) {
            super (name, ext);
        }
        
        boolean isEmpty() {
            return props.isEmpty();
        }
        
        Properties props = new Properties();
        public String add (String val) {
            String key = genKey (val);
            props.put (key, val);
            return key;
        }

        public String add (String key, String val) {
            if (props.containsKey(key)) {
                int ix = 1;
                String s = key + ix;
                while (props.containsKey(s)) {
                    s = key + ix++;
                }
                key = s;
            }
            props.put (key, val);
            return key;
        }
        
        private String genKey (String val) {
            char[] c = val.toUpperCase().toCharArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < c.length; i++) {
                if (Character.isLetter(c[i])) {
                    sb.append (c);
                } else if (' ' == c[i]) {
                    sb.append ('_');
                }
                if (sb.length() > 20 && !props.containsKey (sb.toString())) {
                    break;
                }
            }
            return sb.toString();
        }
    
        public void write(OutputStream os) throws IOException {
            props.store(os, super.toString() + " generated by Wizard Wizard");
        }
    }    
}
