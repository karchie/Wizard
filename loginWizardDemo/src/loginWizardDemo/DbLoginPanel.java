package loginWizardDemo;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 * Panel for collecting information needed to import attribute defininitions
 * from a database table.
 * 
 * @author Stanley@stanleyKnutson.com
 */
public class DbLoginPanel extends WizardPage
{

    static String STEP_ID     = "login";

    static String DESCRIPTION = "Database Login";

    // wizard api requires these two static methods
    public static String getStep()
    {
        return STEP_ID;
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    static Connection           __openConnection = null;

    static final String FAKE_DRIVER_OK = "TEST WIZARD CONNECT OK";
    static final String FAKE_DRIVER_FAILURE = "TEST WIZARD CONNECT FAILURE";
    
    static String               DRIVER_CHOICES[] = {
        "oracle.jdbc.driver.OracleDriver",
        "com.mysql.jdbc.Driver",
        FAKE_DRIVER_OK,
        FAKE_DRIVER_FAILURE
        // we are not testing the
        // jdbc/odbc driver, so
        // don't show it as a choice
        // "sun.jdbc.odbc.JdbcOdbcDriver"
    };

    private final static String DEFAULT_DRIVER   = FAKE_DRIVER_OK;

    // private final static String DEFAULT_URL =
    // "jdbc:oracle:thin:@localhost:1521:ORCL";

    /** Password for database is not persisted outside the app */
    static String               __lastPassword   = "";

    public static void ensureConnectionClosed()
    {
        if (__openConnection != null)
        {
            try
            {
                __openConnection.close();
            }
            catch (SQLException ex)
            {
                // ignore
            }
        }
        __openConnection = null;
    }

    private JComboBox _driverComponent;
    
    private JTextField _hostnameComponent;

    private JTextField _sidComponent;

    private JTextField _portComponent;

    private JTextField _usernameComponent;

    private JTextField _passwordComponent;

    DbLoginPanel       _loginPanel;

    /** Creates new form FirstPage */
    public DbLoginPanel()
    {
        super(STEP_ID, DESCRIPTION, true);

        // construction of the listener will attach it to the components
        // must done be before the components are added to the panel
//        new LoginPanelWizardListener(this);

        initComponents();
    }

    private void initComponents()
    {
        JPanel inner = new JPanel();
        inner.setLayout(new GridLayout(6, 1));
        inner.add(createDriverComponent());
        inner.add(createHostnameComponent());
        inner.add(createPortComponent());
        inner.add(createSidComponent());
        inner.add(createUsernameComponent());
        inner.add(createPasswordComponent());
        inner.setPreferredSize(new Dimension(350, 226));

        // no layout in outer panel, so it won't make the boxes too wide if
        // outer panel is wide
        add(inner);
    }
    public Component createDriverComponent()
    {
        // TODO: should have nicer names ("Oracle 9/10" for example)
        _driverComponent = new JComboBox (DRIVER_CHOICES);
        _driverComponent.setSelectedItem(DEFAULT_DRIVER);
        _driverComponent.setName(LoginConstants.DRIVER_PROPERTY);
        return new LabeledComponent("Driver", _driverComponent);
    }

    public Component createHostnameComponent()
    {
        _hostnameComponent = createTextField();
        // String text = _propertiesManager.getDbProperty(HOSTNAME_PROPERTY,
        // DEFAULT_HOSTNAME);
        // _hostnameComponent.setText(text);
        _hostnameComponent.setName(LoginConstants.HOSTNAME_PROPERTY);
        return new LabeledComponent("Hostname", _hostnameComponent);
    }

    // SID is oracle's term
    public Component createSidComponent()
    {
        _sidComponent = createTextField();
        // String text = _propertiesManager.getDbProperty(SID_PROPERTY,
        // DEFAULT_SID);
        // _sidComponent.setText(text);
        _sidComponent.setName(LoginConstants.SID_PROPERTY);
        return new LabeledComponent("Database System ID", _sidComponent);
    }

    private Component createPasswordComponent()
    {
        _passwordComponent = createPasswordField();
        String text = __lastPassword;
        _passwordComponent.setText(text);
        _passwordComponent.setName(LoginConstants.PASSWORD_PROPERTY);
        return new LabeledComponent("Password", _passwordComponent);
    }

    private Component createPortComponent()
    {
        _portComponent = createTextField();
        // String text = _propertiesManager.getDbProperty(PORT_PROPERTY,
        // DEFAULT_PORT);
        // _portComponent.setText(text);
        _portComponent.setName(LoginConstants.PORT_PROPERTY);
        return new LabeledComponent("Port", _portComponent);
    }

    private Component createUsernameComponent()
    {
        _usernameComponent = createTextField();
        // String text = _propertiesManager.getDbProperty(USERNAME_PROPERTY,
        // null);
        // _usernameComponent.setText(text);
        _usernameComponent.setName(LoginConstants.USERNAME_PROPERTY);
        return new LabeledComponent("Username", _usernameComponent);
    }

    public String getUsername()
    {
        String userName = _usernameComponent.getText();
        return userName;
    }

    protected String validateContents(Component component, Object event)
    {
        if (isBlank(_hostnameComponent.getText()))
        {
            return "Hostname is required";
        }
        if (isBlank(_portComponent.getText()))
        {
            return "Port is required";
        }
        if (isBlank(_sidComponent.getText()))
        {
            return "SID is required";
        }
        if (isBlank(_usernameComponent.getText()))
        {
            return "Username is required";
        }
        if (isBlank(_passwordComponent.getText()))
        {
            return "Password is required";
        }
        return null;
    }

    public WizardPanelNavResult allowNext(String stepName, Map settings, Wizard wizard)
    {
        // we don't know yet if the user can go to the next screen
        // so do the connect a deferred result, to allow for busy indicator 
        return new OpenConnection ();
    }

    protected void renderingPage()
    {
        super.renderingPage();

        Map m = getWizardDataMap();

        valueTo(m, _hostnameComponent);
        valueTo(m, _passwordComponent);
        valueTo(m, _portComponent);
        valueTo(m, _sidComponent);
        valueTo(m, _usernameComponent);
    }

    class OpenConnection extends WizardPanelNavResult
    {
        Connection      _conn       = null;

        String          _errorMessage = null;

        OpenConnection()
        {
            super(true);
        }

        public void start(Map settings, ResultProgressHandle progress)
        {
            progress.setBusy("Connecting...");
            // do connection in background thread
            _conn = makeConnectionInternal(settings);
            
            // the DeferredWizardResult is true if the connection was successful
            if (_conn != null)
            {
                settings.put(LoginConstants.CONNECTION_PROPERTY, _conn);
                
                // track that we did open a connection in case the user cancels
                __openConnection = _conn;
                
                progress.finished(WizardPanelNavResult.PROCEED);
            }
            else
            {
                progress.failed (_errorMessage, true);
            }
        }

        private Connection makeConnectionInternal(Map settings)
        {
            // TODO: should really use values from the settings
            try
            {
                String hostname = _hostnameComponent.getText();
                String sid = _sidComponent.getText();
                String port = _portComponent.getText();

                // result is like "jdbc:oracle:thin:@localhost:1521:ORCL";
                String url = "jdbc:oracle:thin:@" + hostname + ":" + port + ":" + sid;
                String username = _usernameComponent.getText();
                String password = _passwordComponent.getText();

                if (isBlank(username) || isBlank(password)
                    || isBlank(hostname) || isBlank(sid)
                    || isBlank(port))
                {
                    // should not happen, since validateContents should have
                    // happened already
                    _errorMessage = "All input fields are required";
                    return null;
                }
                
                Connection result = loadDriverConnect(url, username, password);
                
                return result;
            }
            catch (Exception ex)
            {
                String message = "Failed to connect:\n" + ex.toString();
                if (ex instanceof SQLException)
                {
                    String errMess = ex.getMessage();
                    if (errMess.indexOf("ORA-01017") >= 0)
                    {
                        message = "Invalid username or password - login is denied.";
                    }
                    else
                    {
                        message = errMess;
                    }
                }
                _errorMessage = message;
                return null;
            }
        }
    }
    
    protected Connection loadDriverConnect (String url, String username, String password)
        throws SQLException
    {
        String driver = (String) _driverComponent.getSelectedItem();

        if (FAKE_DRIVER_OK.equals(driver))
        {
            return fakeDriverOk(url);
        }
        if (FAKE_DRIVER_FAILURE.equals(driver))
        {
            return fakeDriverFailure(url);
        }
        
        try
        {
            Class clas = Class.forName(driver);
        }
        catch (ClassNotFoundException ex)
        {
            //JOptionPane.showMessageDialog(this, "The selected driver is not available\n" + ex.getMessage());
            throw new RuntimeException("The selected driver is not on the classpath: " + driver);
            // return false;
        }
        
        Connection result = DriverManager.getConnection(url, username, password);

        return result;
    }
    
    private Connection fakeDriverOk (String url)
    {
        // simulate network waiting, just sleep for a while
        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException ex)
        { } 
        return new FakeConnection();
    }
    
    private Connection fakeDriverFailure (String url)
        throws SQLException
    {
        fakeDriverOk (url);
        throw new SQLException ("Fake driver timeout connecting to " + url);
    }
    
    static JTextField createTextField ()
    {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension (50, 25));
        return field;
    }

    static JPasswordField createPasswordField ()
    {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension (50, 25));
        return field;
    }

    public static boolean isBlank (String v)
    {
        // normally this would call org.apache.commons.lang.StringUtils.isBlank method
        // but I don't want that dependency in this demo
        if (v == null || v.length() == 0)
        {
            return true;
        }
        // TODO: look for strings containing all spaces  (better to use the jakarta / commons / lang library)
        return false;
    }
    
}
