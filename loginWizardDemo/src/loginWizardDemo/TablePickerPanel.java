package loginWizardDemo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.spi.wizard.WizardPage;

/**
 * Panel for selecting table to use for importing data
 
 * @author Stanley@stanleyKnutson.com
 */
public class TablePickerPanel extends WizardPage
{
    static String STEP_ID = "pickTable";
    
    static String DESCRIPTION = "Select Table";

    // wizard api requires these two static methods
    public static String getStep()
    {
        return STEP_ID;
    }
    public static String getDescription()
    {
        return DESCRIPTION;
    }

    /** Define static logger */

    private JList _tableListComponent;
    private DefaultListModel _tableListModel = new DefaultListModel();

    private boolean _haveSchemas = true;
    private JList _schemasList;
    private DefaultListModel _schemasListModel = new DefaultListModel();    
    Connection _connection;
        
    public TablePickerPanel()
    {
        super(STEP_ID, DESCRIPTION);
        
        setLayout(new BorderLayout());
        JComponent component = createSchemasComponent();
        add(component, BorderLayout.NORTH);
        add(createListComponent(), BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(450, 600));
        
    }

    protected void renderingPage()
    {
        super.renderingPage();

        _connection = (Connection) getWizardData(LoginConstants.CONNECTION_PROPERTY);
        if (_connection == null)
        {
            throw new RuntimeException ("Can't initialize TablePickerPanel without a connection");
        }
        
        // loading schema and tables will trigger the select action, we don't want to trash previous selection
        String oldSchema = (String) getWizardData(LoginConstants.SCHEMA_PROPERTY);
        String oldTable = (String) getWizardData(LoginConstants.SELECTED_TABLE_PROPERTY);
        
        _loadSchemas();

        if (oldSchema != null)
        {
            _selectSchema(oldSchema);
        }
        else
        {
            String username = (String) getWizardData(LoginConstants.USERNAME_PROPERTY);
            _selectSchema (username);
        }
        
        _rebuildList();  // load with tables for the current user
        
        if (oldTable != null)
        {
            _tableListComponent.setSelectedValue(oldTable, true);
        }
        
    }

    // create the schemas components but don't load them since we don't yet have connection
    private JComponent createSchemasComponent()
    {
        _schemasList = new JList();
        _schemasList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _schemasList.setName(LoginConstants.SCHEMA_PROPERTY);
        
        //  if (_haveSchemas)   // assume true for now, we only support oracle
        {
            JScrollPane scroller = new JScrollPane (_schemasList);
            JComponent result = new LabeledComponent("Schema Name", scroller);
            result.setPreferredSize(new Dimension(450, 105));
            return result;
        }
        // return new JLabel ("This data source does not support schema browsing");
    }

    private Component createListComponent()
    {
        // double-click action not implemented rignt now
        // PickTableAction action = new PickTableAction();
        _tableListComponent = new JList();
        _tableListComponent.setModel(_tableListModel);
        _tableListComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _tableListComponent.setName(LoginConstants.SELECTED_TABLE_PROPERTY);

        _schemasList.addListSelectionListener(new UpdateListAction());

        // we don't load the list yet, the connection may not be valid
        
        JScrollPane scroller = new JScrollPane(_tableListComponent);
        _tableListComponent.addListSelectionListener(new SelectTableAction());
        
        return new LabeledComponent("Tables", scroller);
    }

    void _loadSchemas()
    {
        _schemasListModel = new DefaultListModel();

        if (_connection instanceof FakeConnection)
        {
            _schemasList.setModel(_schemasListModel);
            _haveSchemas = false;
            return;
        }
        
        try
        {
            DatabaseMetaData md = _connection.getMetaData(); 
            ResultSet data = md.getSchemas();
            ResultSetMetaData rsmd = data.getMetaData();
            
            while (data.next())
            {
                String schema = data.getString("TABLE_SCHEM");
                
                _schemasListModel.addElement(schema);
            }
            
            _haveSchemas = true;
        }
        catch (SQLException ex)
        {
            // schema support is optional
            //    __logger.error("Can't load schemas", ex);
            _haveSchemas = false;
        }
        _schemasList.setModel(_schemasListModel);
    }

    private void _selectSchema (String username)
    {
        if (!_haveSchemas || username == null)
        {
            return;
        }
        int count = _schemasListModel.getSize();
        for (int i = 0; i < count; ++i)
        {
            String schemaName = (String) _schemasListModel.getElementAt(i);
            if (username.equalsIgnoreCase(schemaName))
            {
                _schemasList.getSelectionModel().setLeadSelectionIndex(i);
                _schemasList.getSelectionModel().setSelectionInterval(i, i);
                // _schemasList.scrollToVisible(i);
                _rebuildList();
                return;
            }
        }
        return;
    }
    
    String getSchemaName()
    {
        int row = _schemasList.getSelectedIndex();
        if (row < 0)
        {
            return null;
        }
        String schema = (String) _schemasListModel.getElementAt(row);
        return schema;
    }

    void _rebuildList()
    {
        try
        {

            // we don't allow schema to be empty!
            String schemaName = getSchemaName();
            if ((_haveSchemas && (schemaName == null
                        || schemaName.length() == 0)))
            {
                _tableListModel.clear();
                return;
            }

            if (_connection instanceof FakeConnection)
            {
                _tableListModel.clear();
                _tableListModel.addElement("Fake_table_name");
                return;
            }
            
            DatabaseMetaData md = _connection.getMetaData();

            // no filtering yet
            String tableNamePattern = null;
            String tableTypes[] = null;
            ResultSet tbdata =
                md.getTables(null, schemaName, tableNamePattern, tableTypes);

            Vector tableNames = new Vector();
            while (tbdata.next())
            {
                String table = tbdata.getString("TABLE_NAME");
                tableNames.add(table);
            }
            Collections.sort(tableNames);
            _tableListModel.clear();
            // silly default table list model does not have addAll
            for (Iterator iter = tableNames.iterator(); iter.hasNext();)
            {
                String tbl = (String) iter.next();
                // ignore oracle internal tables
                if (tbl.startsWith("BIN$") || tbl.startsWith("DR$"))
                {
                    continue;
                }
                _tableListModel.addElement(tbl);
            }
        }
        catch (SQLException ex)
        {
            logError("Can't load tables", ex);
            _tableListModel.clear();
        }
        _tableListComponent.clearSelection();
    }
    
    private abstract class TableSelectionAction implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent event) {
            if (!event.getValueIsAdjusting()) {
                selectionChanged (event);
            }
        }
        protected abstract void selectionChanged(ListSelectionEvent event);
    }
    
    private class SelectTableAction extends TableSelectionAction
    {
        public void selectionChanged(ListSelectionEvent ev)
        {
            String table = (String) _tableListComponent.getSelectedValue();
            putWizardData(LoginConstants.SELECTED_TABLE_PROPERTY, table);
        }
    }

    private class UpdateListAction extends TableSelectionAction
    {
        public void selectionChanged(ListSelectionEvent ev)
        {
            _rebuildList();
            String schema = (String) _schemasList.getSelectedValue();
            putWizardData(LoginConstants.SCHEMA_PROPERTY, schema);
        }
    }

    // double click selects and goes to next
    // not implemented right now
//    private class PickTableAction extends AbstractAction
//    {
//        public void actionPerformed(ActionEvent ev)
//        {
//            String table = (String) _tableListComponent.getSelectedValue();
//            putWizardData(LoginConstants.SELECTED_TABLE_PROPERTY, table);
//        }
//    }

    protected String validateContents(Component component, Object event)
    {
        if (_tableListComponent != null && _tableListComponent.getSelectedIndex() < 0)
        {
            return "Select a table to continue";
        }
        return null;
    }

// helper
//    static void describeResultSet (String title, ResultSet rs)
//        throws SQLException
//        {
//            ResultSetMetaData rdmd = rs.getMetaData();
//            int numcols = rdmd.getColumnCount();
//            System.out.println(title + " has " + numcols + " columns");
//            for (int i = 0; i < numcols; i++)
//            {
//                String name = rdmd.getColumnName(i+1);
//                String type = rdmd.getColumnClassName(i+1);
//                System.out.println (i + "  " + name + "  " + type);
//            }
//        }
    
    static void logError (String where, Throwable th)
    {
        System.err.println(where);
        th.printStackTrace(System.err);
    }

}
