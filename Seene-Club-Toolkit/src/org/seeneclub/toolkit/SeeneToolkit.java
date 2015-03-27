package org.seeneclub.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SeeneToolkit implements Runnable, ActionListener, MouseListener {
	
	JFrame mainFrame = new JFrame("...::: Seene-Club-Toolkit-GUI :::...");
	
	// We need a local storage for the Seenes
	SeeneStorage storage = new SeeneStorage();
  	Boolean storageOK = false;
	
	// Settings Dialog
	File configDir = null;
    File configFile = null;
	JDialog settingsDialog = new JDialog();
    String seeneUser = new String();
    String seenePass = new String();
    String seeneAPIid = new String();
	
	// Task Menu Items
    JMenuItem taskBackupPublic = new JMenuItem("Backup public Seenes");
    JMenuItem taskBackupPrivate = new JMenuItem("Backup private Seenes");
    
    // Tests Menu Items
    JMenuItem testDoLogin = new JMenuItem("Test Login");
    
    // method main - all begins with a thread!
	public static void main(String[] args) {
		new Thread(new SeeneToolkit()).start();
	}

    private void doTestLogin() {
		try {
			SeeneAPI.Token token = SeeneAPI.login(seeneAPIid,seeneUser,seenePass);
					
			System.out.println(token.api_token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
    
    // @PAF prepared two methods for the Backup Tasks. They are called by selecting the tasks from the menu.
    private void doTaskBackupPublicSeenes() {
    	System.out.println("Public Seenes will go to " + storage.getPublicDir());
    	
    }
    
    private void doTaskBackupPrivateSeenes() {
    	System.out.println("Private Seenes will go to " + storage.getPrivateDir());
    	
    }
    
	@Override
	public void run() {
		
		configDir = new File(System.getProperty("user.home") + File.separator + ".seene-club");
    	if (configDir.exists() || configDir.mkdirs()) {
    		configFile = new File(configDir + File.separator + "configuration");
    		if(!configFile.exists()) {
    			// Show dialog for directory browsing
    			JFileChooser chooser = new JFileChooser();
    			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    			int fileChooserReturnValue = chooser.showDialog(null,"select directory to store your backuped seenes");
    	        
    	        // Check if chosen or canceled
    	        if(fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
    	            System.out.println("user selected path: " + chooser.getSelectedFile().getPath());
    	            // Write path in configuration file
    	            PrintWriter writer;
					try {
						writer = new PrintWriter(configFile);
						writer.println("storage=" + chooser.getSelectedFile().getPath());
		    			writer.close();
		    			System.out.println("new configuration file " + configFile.getPath() + " written!");
		    			
		        		showSettingsDialog();
		        		
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //try/catch
    	        } else {
    	        	System.out.println("user canceled selection!");
    	        	System.exit(0);
    	        } // if ... JFileChooser.APPROVE_OPTION
    		} else {
    			readConfiguration(configFile);
       		} 
   		}
	
		mainFrame.setSize(1024,768);
		
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	 System.exit(0);
		    }
		});
		
		// menu bar
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("Seene-Club");
        
        JMenuItem itemSettings = new JMenuItem("Settings");
        itemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showSettingsDialog();
            }
        });
        
        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });
        
        fileMenu.add(itemSettings);
        fileMenu.add(itemExit);
        
        JMenu taskMenu = new JMenu("Tasks");
        
        taskBackupPublic.addActionListener(this);
        taskBackupPrivate.addActionListener(this);
        
        taskMenu.add(taskBackupPublic);
        taskMenu.add(taskBackupPrivate);
        
        JMenu testMenu = new JMenu("Tests");
        testDoLogin.addActionListener(this);
        
        testMenu.add(testDoLogin);
                
        bar.add(fileMenu);
        bar.add(taskMenu);
        bar.add(testMenu);
        
        mainFrame.setJMenuBar(bar);

		mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        
	}
	
	// the action listener for the GUI
	public void actionPerformed(ActionEvent arg0) {
		
		if(arg0.getSource() == this.taskBackupPublic) {
			doTaskBackupPublicSeenes();
		} else if(arg0.getSource() == this.taskBackupPrivate) {
			doTaskBackupPrivateSeenes();
		} else if(arg0.getSource() == this.testDoLogin) {
	    	doTestLogin();
	    }
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private void showSettingsDialog() {
		
		readConfiguration(configFile);
		
    	settingsDialog.setTitle("Seene-Club Settings");
    	settingsDialog.setSize(400, 160);
    	settingsDialog.setModal(true);
    	
    	JPanel gridPanel = new JPanel();
    	gridPanel.setLayout( new java.awt.GridLayout( 5, 2 ) );
    	
    	JLabel labelLocalStorage = new JLabel(" Local Storage: ");
    	JTextField tfLocalStorage = new JTextField(storage.getPath());
    	
    	JLabel labelUsername = new JLabel(" Seene Username: ");
    	JTextField tfUsername = new JTextField(seeneUser);
    	
    	JLabel labelPassphrase = new JLabel(" Seene Password: ");
    	JPasswordField tfPassphrase = new JPasswordField(10);
    	if (seenePass.length()>0) tfPassphrase.setText("{unchanged}");
    	
    	tfLocalStorage.setEnabled(false);
  	
    	JButton buttonOK = new JButton("OK");
    	buttonOK.addActionListener(new java.awt.event.ActionListener() {
            @SuppressWarnings("deprecation")
			public void actionPerformed(java.awt.event.ActionEvent e) {
            	Boolean usernameOK = false;
            	Boolean storageOK = false;
            	
            	if (tfUsername.getText().length() > 0) usernameOK=true;
            	if (tfLocalStorage.getText().length() > 0) storageOK=true;
            	            	
            	if ((usernameOK) && (storageOK)) {
            		PrintWriter writer;
					try {
						// write configuration file
						writer = new PrintWriter(configFile);
						if (seeneAPIid.length() > 0) {
							writer.println("api_id=" + seeneAPIid);
						} else {
							writer.println("api_id=<insert Seene API ID here>");
						}
						writer.println("storage=" + tfLocalStorage.getText());
						writer.println("username=" + tfUsername.getText());
						if (tfPassphrase.getText().length() > 0) {
							 if (!tfPassphrase.getText().equals("{unchanged}")) writer.println("passphrase=" + XOREncryption.xorIt(tfPassphrase.getText()));
							 if (tfPassphrase.getText().equals("{unchanged}")) writer.println("passphrase=" + XOREncryption.xorIt(seenePass));
						}
						
		    			writer.close();
		    			System.out.println("new configuration file " + configFile.getPath() + " written!");
		    			
		    			readConfiguration(configFile);
		    			settingsDialog.remove(gridPanel);
		    			settingsDialog.dispose();

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} //try/catch
            		
            	} else {
            		JOptionPane.showMessageDialog(null, "Your settings are incomplete!");
            	}
            }
        });
    	
    	JButton buttonCancel = new JButton("cancel");
    	buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	settingsDialog.remove(gridPanel);
            	settingsDialog.dispose();
            }
        });
   
     	gridPanel.add(labelLocalStorage);
    	gridPanel.add(tfLocalStorage);
    	gridPanel.add(labelUsername);
    	gridPanel.add(tfUsername);
    	gridPanel.add(labelPassphrase);
    	gridPanel.add(tfPassphrase);
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(buttonOK);
    	gridPanel.add(buttonCancel);
    	
    	settingsDialog.add(gridPanel);
    	//settingsDialog.pack();
    	
    	settingsDialog.setVisible(true);
    }
	
	private boolean readConfiguration(File cf) {
    	if(cf.exists() && !cf.isDirectory()) {
			System.out.println("reading application configuration...");
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(cf));
				String line;
    			while ((line = br.readLine()) != null) {
    				if (line.substring(0, 7).equalsIgnoreCase("api_id=")) {
    					System.out.println("configured api_id is: " + line.substring(7));
    					seeneAPIid = line.substring(7);
    				}
    				if (line.substring(0, 8).equalsIgnoreCase("storage=")) {
    					System.out.println("configured starage path: " + line.substring(8));
    					storage.setPath(line.substring(8));
    					storageOK = storage.initializer();
    				}
    				if (line.substring(0, 9).equalsIgnoreCase("username=")) {
    					System.out.println("configured username: " + line.substring(9));
    					seeneUser = line.substring(9);
    				}
    				if (line.substring(0, 11).equalsIgnoreCase("passphrase=")) {
    					System.out.println("configured passphrase: " + line.substring(11));
    					seenePass = XOREncryption.xorIt(line.substring(11));
    				}
    				
    			}
    			br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} // if (f.exists()...
    	return storageOK;
    }

}

