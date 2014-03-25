
import java.awt.CheckboxMenuItem;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/*
 
Created on January 2, 2000
Ben Resner
benres@media.mit.edu
 
Simple terminal program.  Good example for the SimpleSerial class.
There's a fair amount of code that's irrelevant to the serial port.
For a first-pass understanding, search for 'm_SerialPort'
 
For an even simpler example, see the main() rotine of SimpleSerial.java
 */


public class JavaTerm implements ItemListener, ActionListener {
    
    private JFrame                   m_Frame;
    private Container                cp;
    private  JFileChooser             fileopener=new JFileChooser(); // main window frame
    private  JScrollPane               TextInputSc;
    private  JScrollPane               TextOutputSc;
    private  JTextArea                m_TextInput;                                        // bottom half of window where text gets read in
    private  JTextArea                m_TextOutput;
    private BufferedReader             Bread;
    private BufferedWriter             Bwrite;
    private  JLabel                  OutputLabel=new JLabel("-Output-");
    private  JLabel                  inputLabel=new JLabel("-Input-");
    private JButton                  savebutt=new JButton("save");
    private JLabel                   namelabel=new JLabel("Filename :");
    private JTextField                nameFiled=new JTextField(20) ;
        String      inputString ="";
    // top half of window where text gets sent out
    private JMenuItem file_elem =new JMenuItem("Open");
    private  JCheckBoxMenuItem       m_PortMenuItem[]= new JCheckBoxMenuItem [2];         // com port menu items
    private   JMenuItem                m_ClearMenuItem = new JMenuItem("Clear Buffers");    // 'clear buffers' menu item
//     JCheckBoxMenuItem        m_ModeMenuItem[] = new   JCheckBoxMenuItem[2];
    private    SimpleSerial            m_SerialPort = null;                                // Serial port
    private    int                     m_PortIndex;                                        // Which comm port to use (1-based value -- there is no Comm0)
    private   boolean                 m_IsNative = true;                                  // Use local code, or use JavaComm from Sun
    private   static final String     m_PrefsFileName = new String("JavaTerm.pref");      // name of preferences file
    
    public static void main(String[] args) {
        // Create instance of JavaTerm().  No reason why we shouldn't be able to create multiple instances.
        // The only wrinkle would be making the various prefs files unique.
        new JavaTerm();
    }
    
    // Init the serial port and associated Input/Output streams
    private void initSerialPort() throws IOException {
        
        // If serial port was previously opened, close it now
        // Most applications open serial port, and never need to close it again.
        if (m_SerialPort != null) {
            m_SerialPort.close();
            m_SerialPort = null;
        }
        
        // New instance of the serial port.
        if (m_IsNative) {
            m_SerialPort = new SimpleSerialNative(m_PortIndex);
        }
        
        
        // If there's an error, throw an exception
        if (!m_SerialPort.isValid()) {
            throw (new IOException("Serial port not opened"));
        }
    }
    
    // Constructor
    JavaTerm() {
        int                     ii;
        JMenu                    menu;
        JMenuBar                 menuBar;
        
        // Load in the port number from the last time we ran
        try {
            // try opening the file JavaTerm.pref
            DataInputStream prefs = new DataInputStream(new FileInputStream(m_PrefsFileName));
            // Read in serial port index
            m_PortIndex = prefs.readInt();
            m_IsNative = prefs.readBoolean();
            // check to make sure the value we read is valid
            if (m_PortIndex < 0 || m_PortIndex >= m_PortMenuItem.length) {
                throw new IOException(m_PrefsFileName + " is corrupt");
            }
            // init the serial port
            initSerialPort();
        }
        // We'll get here if the file doesn't exist because it's the first time running this program,
        // it can't be found / opened, or the requested serial port can't be opened
        catch(IOException e) {
            System.out.println("preferences file 'JavaTerm.pref' not found / didn't open or there was a problem opening serial port.  Searching for serial port");
            
            // Open the first available serial port.
            // This code can be adapted to provide a list of available serial ports.
            find_open_serial_port:
                for (ii = 0; ii < m_PortMenuItem.length; ii++) {
                try {
                    m_PortIndex = ii + 1;             // This is the serial port we want to open
                    initSerialPort();                       // Try opening this serial port.  Throws exception if there's a problem
                    System.out.println("Opening serial port Comm" + m_PortIndex);
                    break find_open_serial_port;            // If we haven't thrown an exception, we're done
                } catch (IOException ee) {                     // wind up here if initSerialPort() above has a problem
                    if (ii == m_PortMenuItem.length - 1) {
                        System.out.println("Couldn't open any serial ports");
                        System.exit(0);                     // can't open any serial ports.
                    }
                }
                }
        }
        
        // new our window frame
        m_Frame = new JFrame("Javaterm");
        
        // set frame parameters
        
        
        // add the menu.
        menuBar = new JMenuBar();
        menu = new JMenu("Sitting");
        file_elem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int choice = fileopener.showOpenDialog(null);
                if(choice == JFileChooser.APPROVE_OPTION) {
                    File myfile = fileopener.getSelectedFile();
                    
                    //     File_out=new FileOutputStream(myFile);
                    //   Data_out=new DataOutputStream(File_out);
                    try {//myfile.getCanonicalPath(myfile);
                        //     Bread=new BufferedReader(new FileReader(myfile));
                        FileInputStream  File_in=new FileInputStream(myfile);
                        DataInputStream Data_in =new DataInputStream(File_in);
                        m_TextOutput.setText(" ");
                        byte ch[]=new byte[50000];
                        int ii=0;
                        while(ii!=-1) {
                            ii=Data_in.read(ch);
                            try { //    if(str!=-1)
                            for(int j=0;j<ii;j++) {
                                m_SerialPort.writeByte(ch[j]);
                                 Thread.sleep(18);
                            }
                           
                                Thread.sleep(0);
                            } catch (InterruptedException ex) {
                                Data_in.close();
                                File_in.close();
                                m_TextOutput.append("\nFile Sent !");
                                ex.printStackTrace();
                            }
                            
                        }
                             m_TextOutput.append("\nFile Sent !");
                        
                        
                    } catch (IOException ex) {
                        
                        m_TextOutput.append("\nFile Sent !");
                        // ex.printStackTrace();
                    }
                    
                    
                }
            }
        });
        menu.add(file_elem);
        for (ii = 0; ii < m_PortMenuItem.length; ii++) {
            menu.add(m_PortMenuItem[ii] = new  JCheckBoxMenuItem("Com " + (ii + 1), false));
            m_PortMenuItem[ii].addItemListener(this);
        }
        m_PortMenuItem[m_PortIndex - 1].setState(true);
        
        menuBar.add(menu);
        
        menu = new JMenu("Clear");
        menu.add(m_ClearMenuItem);
        m_ClearMenuItem.addActionListener(this);
        menuBar.add(menu);
        
        
        
        m_Frame.setJMenuBar(menuBar);
        cp=m_Frame.getContentPane();
        
        cp.setLayout(new FlowLayout());
        cp.add(OutputLabel);
        m_TextOutput = new JTextArea(10,40);
        TextOutputSc=new JScrollPane( m_TextOutput,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        cp.add(TextOutputSc);
        // create where the text areas goes
        cp.add(inputLabel);
        m_TextInput = new JTextArea(10,40);
        TextInputSc=new JScrollPane( m_TextInput,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        m_TextInput.setEditable(false);
        cp.add(TextInputSc);
        
        
        
        
        
        // position the text areas
//        updateWindowComponents();
        
        // When a key is pressed, send that keystroke to the serial port
        m_TextOutput.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent  e) {
                //     System.out.println("You Typed a character ");
                byte    tempByte = (byte)e.getKeyChar();
                m_SerialPort.writeByte(tempByte);     // <-- Here's where the data gets written
                // DONT use writeChar().  It's a two byte char
                // and has screwey results.
                
                // HACKISH:  Need to add newline to carriage returns for proper operation
                // You may not need this.
                if (tempByte == '\r') {
                    m_SerialPort.writeByte((byte)'\n');
                }
            }
        } );
        
        // Make sure the focus stays on the output window.  If input pane gets focus, set focus back to output pane
        m_TextInput.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                m_TextOutput.requestFocus();
            }
        });
        
        // If user clicks close box, exit this program
        m_Frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Write the current serial port to disk
                writePrefs();
                System.exit(0);
            }
        } );
        
        // If user resizes the window, need to reposition the text areas.
        m_Frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateWindowComponents();
            }
        });
        savebutt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File myfile=new File(nameFiled.getText());
                FileOutputStream File_out;
                try {
                    File_out = new FileOutputStream(myfile);
                    DataOutputStream     Data_out=new DataOutputStream(File_out);
                    BufferedWriter myWrit=new BufferedWriter(new FileWriter(myfile));
                        myWrit.write(inputString);
                    myWrit.flush();
                    myWrit.close();
                    Data_out.close();
                    File_out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                
            }
        });
        
        nameFiled.setText("rec.txt");
        cp.add(namelabel);
        cp.add(nameFiled);
        cp.add(savebutt);
        
        m_Frame.setSize(500, 600);
        m_Frame.setLocation(100, 100);
        m_Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_Frame.setVisible(true);
        
        // Infinite loop.  WindowListener above will break us out of loop with call to System.exit(0);
        for (;true;) {
            // Get any pending characters from serial port.
            // Returns empty string if there's nothing to read.
            // This is in contrast to readByte() which patiently waits for data.
            
            if (m_SerialPort != null) {
                 inputString+= m_SerialPort.readString();
           
           
            }
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        
        
    }
    
    // When user changes ComPort from menu, this code gets hit.
    public void itemStateChanged(ItemEvent e) {
        int         ii;
        int         prevSerialPort = m_PortIndex;
        boolean     prevIsNative = m_IsNative;
        boolean     reinitPort = false;
        
        // find which com port was requested
        find_menu_com_port:
            for (ii = 0; ii < m_PortMenuItem.length; ii++) {
                if (e.getSource() == m_PortMenuItem[ii]) {
                    m_PortIndex = ii + 1;
                    reinitPort = true;
                    break find_menu_com_port;
                }
            }
            
            // Maybe it's the mode that's changed.
            
            m_IsNative = true;
            
            
            
            if (reinitPort) {
                try {
                    initSerialPort();
                } catch (IOException ex) {
                    System.out.println("Requsted serial port couldn't be opened -- reverting");
                    m_PortIndex = prevSerialPort;
                    m_IsNative = prevIsNative;
                    try {
                        initSerialPort();
                    } catch (IOException ex2) {
                        System.out.println("Couldn't restore previous serial port.  You're hosed");
                        System.exit(0);
                    }
                }
            }
            
            // Make sure menu checkmarks match current com port.
            for (ii = 0; ii < m_PortMenuItem.length; ii++) {
                m_PortMenuItem[ii].setState( (m_PortIndex == ii + 1) ? true : false);
            }
            
///            m_ModeMenuItem[0].setState(m_IsNative);
            
            
            writePrefs();
    }
    
    private void writePrefs() {
        try {
            DataOutputStream prefs = new DataOutputStream(new FileOutputStream(m_PrefsFileName));
            prefs.writeInt(m_PortIndex);
            prefs.writeBoolean(m_IsNative);
        } catch (IOException ee) {}
    }
    
    // If user selects "Clear Buffers" we wind up here.
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == m_ClearMenuItem) {
            m_TextInput.setText("");
            m_TextOutput.setText("");
        }
    }
    
    // Updates input and output text areas.
    private void updateWindowComponents() {
        //  Insets      frameInsets = m_Frame.getInsets();
        ///  Dimension   dim = m_Frame.getSize();
        
        //  dim.height -= (frameInsets.bottom + frameInsets.top);
        //  dim.width -= (frameInsets.right + frameInsets.left);
        
        // m_TextInput.setSize(dim.width, dim.height / 2);
        // m_TextInput.setLocation(frameInsets.left, frameInsets.top + dim.height / 2);
        
        // m_TextOutput.setSize(dim.width, dim.height / 2);
        // m_TextOutput.setLocation(frameInsets.left, frameInsets.top);
        
        m_TextOutput.requestFocus();
    }
};