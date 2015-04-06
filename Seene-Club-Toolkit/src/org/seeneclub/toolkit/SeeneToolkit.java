package org.seeneclub.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.seeneclub.domainvalues.LogLevel;
import org.seeneclub.toolkit.SeeneAPI.Token;

public class SeeneToolkit implements Runnable, ActionListener, MouseListener {
	
	public static final String APPLICATION_LOG_MODE = LogLevel.debug + 
													  LogLevel.info +
													  LogLevel.warn +
													  LogLevel.error +
													  LogLevel.fatal;
	
	JFrame mainFrame = new JFrame("...::: Seene-Club-Toolkit-GUI :::...");
	
	// We need a local storage for the Seenes
	SeeneStorage storage = new SeeneStorage();
  	Boolean storageOK = false;
  	
  	// GUI-Panels
	JPanel panelWest = new JPanel();
	JPanel panelWestNorth = new JPanel();
	JPanel panelWestSouth = new JPanel();
	// Seene listview goes to panelWestSouth and that should be scrollable!
	JScrollPane scrollWestSouth = new JScrollPane (panelWestSouth, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel panelEastNorth = new JPanel();
	JPanel panelEastSouth = new JPanel();
	
	// Elements for Panel WestNorth (seene pool selection)
	JToggleButton btPoolPublicSeenes = new JToggleButton("public");
	JToggleButton btPoolPrivateSeenes = new JToggleButton("private");
	JToggleButton btPoolLocalSeenes = new JToggleButton("local");
	
	// Elements for Panel EastSouth (log output)
    static JTextArea logOutput = new JTextArea();
    JScrollPane logOutputScrollPane = new JScrollPane(logOutput);
	
	// Settings Dialog
	static File configDir = null;
    static File configFile = null;
	JDialog settingsDialog = new JDialog();
    static String seeneUser = new String();
    static String seenePass = new String();
    static String seeneAPIid = new String();
	
	// Task Menu Items
    JMenuItem taskBackupPublic = new JMenuItem("Backup public Seenes");
    JMenuItem taskBackupPrivate = new JMenuItem("Backup private Seenes");
    
    // Tests Menu Items
    JMenuItem testDoLogin = new JMenuItem("Test Login");
    
    // method main - all begins with a thread!
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		
		configDir = new File(System.getProperty("user.home") + File.separator + ".seene-club");
		if (configDir.exists() || configDir.mkdirs()) {
			configFile = new File(configDir + File.separator + "configuration");
		}
		
		Boolean commandLineUsed = false;
		
		// command line parsing (using Apache commons CLI)
		CommandLineParser parser = new GnuParser();
		
		Options options = new Options();
		
		Option backupOption = OptionBuilder.withLongOpt("backup")
										   .withDescription("backup public or private Seenes from Server)")
										   .hasArg()
										   .withArgName("VISIBILITY")
										   .create("b");
		
		Option userOption   = OptionBuilder.withLongOpt("username")
										   .withDescription("Seene Username)")
				   						   .hasArg()
				   						   .withArgName("USERNAME")
				   						   .create("u");
		
		Option passOption   = OptionBuilder.withLongOpt("password")
					 		 			   .withDescription("Seene Password)")
					 		 			   .hasArg()
					 					   .withArgName("PASSWORD")
					 					   .create("p");
		
		Option targetOption = OptionBuilder.withLongOpt("output-target")
				 						   .withDescription("target directory for the backup)")
				 						   .hasArg()
				 						   .withArgName("PATH")
				 						   .create("o");
		
		options.addOption(backupOption);
		options.addOption(userOption);
		options.addOption(passOption);
		options.addOption(targetOption);
		
		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );

		    if (line.hasOption("backup")) {
		    	commandLineUsed = true;
		    	// handle public backup. No Login is required.
		    	if (line.getOptionValue("backup").equalsIgnoreCase("public")) {
		    		if (line.hasOption("username")) {
		    			seeneUser = line.getOptionValue("username");
		    		} else {
		    			String errorText = new String("for public backup the Seene username is required!");
		    			throw new org.apache.commons.cli.ParseException(errorText);
		    		}
		    		String targetDir = line.hasOption("output-target")
		    			? line.getOptionValue("output-target")
		    			: System.getProperty("user.dir"); // do the backup to current working dir, if no output-target is given.
	    			doTaskBackupPublicSeenes(
	    					new File(targetDir),
		    				new LogReporter());
		    	} // handle private backup. Login IS required. 
		    	else if (line.getOptionValue("backup").equalsIgnoreCase("private")) {
		    		if (line.hasOption("username")) {
		    			seeneUser = line.getOptionValue("username");
		    			if (line.hasOption("password")) {
		    				seenePass = line.getOptionValue("password");
		    			} else {
		    				// no password given? -> fetch password from console.
		    				Console c = System.console();
		    				if (c == null) {
		    			           log("No console.",LogLevel.fatal);
		    			           System.exit(1);
		    			    }
		    				char consolePass[] = c.readPassword(seeneUser + "'s password: ");
		    				seenePass = new String(consolePass);
		    			}
		    			// because we never put the Seene-API-ID in the code, we read from file!
	    				seeneAPIid = getParameterFromConfiguration(configFile,"api_id");
			    		String targetDir = line.hasOption("output-target")
				    			? line.getOptionValue("output-target")
				    			: System.getProperty("user.dir"); // do the backup to current working dir, if no output-target is given.
				    	doTaskBackupPrivateSeenes(
		    					new File(targetDir),
			    				new LogReporter());
		    		} else {
		    			String errorText = new String("for private backup the Seene credentials are required!");
		    			throw new org.apache.commons.cli.ParseException(errorText);
		    		}
		    		
		    	} else {
		    		StringBuffer errorText = new StringBuffer(line.getOptionValue("backup"));
		    		errorText.append(" unknown backup option. Try \"private\" or \"public\"");
		    		throw new org.apache.commons.cli.ParseException(errorText.toString());
		    	}
		    }
		} catch( org.apache.commons.cli.ParseException exp ) {
			commandLineUsed = true;
		    log("parameter exception: " + exp.getMessage() , LogLevel.error);
		}

		// Start GUI only if NO command line argument is used!
		if (!commandLineUsed) new Thread(new SeeneToolkit()).start();
	}

    private void doTestLogin() {
		try {
			SeeneAPI.Token token = SeeneAPI.login(seeneAPIid,seeneUser,seenePass);
					
			log(token.api_token,LogLevel.info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
    
    private interface Reporter {
    	void report(String what);

		void planSteps(String what, int totalSteps);

		void doneSteps(String what, int doneSteps);
    }
    
    private static class LogReporter implements Reporter {

		@Override
		public void report(String what) {
    		log(what, LogLevel.info);
		}

		@Override
		public void planSteps(String what, int totalSteps) {
    		log(String.format("%s (planning %d)", what, totalSteps), LogLevel.info);
		}

		@Override
		public void doneSteps(String what, int doneSteps) {
    		log(String.format("%s (%d done)", what, doneSteps), LogLevel.info);
		}
    	
    }

    // TODO @Mathias, here's just clone, please hack it to be something useful?
    private static class GUIReporter implements Reporter {

		@Override
		public void report(String what) {
    		log(what, LogLevel.info);
		}

		@Override
		public void planSteps(String what, int totalSteps) {
    		log(String.format("%s (planning %d)", what, totalSteps), LogLevel.info);
		}

		@Override
		public void doneSteps(String what, int doneSteps) {
    		log(String.format("%s (%d done)", what, doneSteps), LogLevel.info);
		}
    	
    }
    
    // @PAF I prepared two methods for the Backup Tasks. 
    // They are called by selecting the tasks from the menu or from command line.
    // example: java -jar seene-club-toolkit.jar -b public -u paf
    private static void doTaskBackupPublicSeenes(File targetDir, Reporter reporter) {
    	try {
    		log("Public Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    	
    		reporter.report("Resolving name to id");
    		
			String userId = SeeneAPI.usernameToId(seeneUser);
		
			log("Seene user: " + userId, LogLevel.debug);

			//@PAF let's start with lesser seenes for testing.
			//perhaps we can later determine which Seenes are already backup and which are not
			//so we can optimize the runtime for following backups
			
			//@Mathias, agreed
			//it is very important to complete an aborted download,
			//so user will not feel "it's OK" when it is not.
			//One way to do it, is just to download to a drafts/ subfolder
			//And only when 100% downloaded OK, move it (atomic operation) one level up
			//And then, if it's there, we may safely skip the download
			//TODO that (below)
			int last = 5;
			reporter.report("Getting index of last " + last + " public seenes");
			List<SeeneObject> index = SeeneAPI.getPublicSeenes(userId, last);
			
			reporter.planSteps("Downloading few last public seenes (not ALL)", last);
			int i = 0;
			for(SeeneObject o : index) {
				downloadSeene(o,targetDir);
				reporter.doneSteps("Downloaded", ++i);
			}

			log("Done",LogLevel.info);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // example: java -jar seene-club-toolkit.jar -b private -u paf -o /home/paf/myPrivateSeenes
    private static void doTaskBackupPrivateSeenes(File targetDir, Reporter reporter) {
    	try {
        	log("Private Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    	
    		reporter.report("Resolving name to id");
    		
			String userId = SeeneAPI.usernameToId(seeneUser);
		
			log("Seene user: " + userId, LogLevel.debug);

			Token token;
			{
				// @TODO Mathias, we may cache the token someday
				reporter.report("Logging in");
				token = SeeneAPI.login(seeneAPIid, seeneUser, seenePass);
			}
					
			int last = 5;
			reporter.report("Getting index of last " + last + " private seenes");
			List<SeeneObject> index = SeeneAPI.getPrivateSeenes(token, userId, last);
			
			reporter.planSteps("Downloading few last private seenes (not ALL)", last);
			int i = 0;
			for(SeeneObject o : index) {
				downloadSeene(o,targetDir);
				reporter.doneSteps("Downloaded", ++i);
			}

			log("Done",LogLevel.info);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // downloads a Seene to a target Directory
    private static boolean downloadSeene(SeeneObject sO, File sF) {
    	Boolean successTexture = false;
    	Boolean successModel = false;
    	SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    	
    	String folderName = new String(sdf.format(sO.getCaptured_at()) + " " + sO.getCaption().replaceAll("\n",  " "));
    	File seeneFolder = new File(sF.getAbsolutePath() + File.separator + folderName);
    	
    	if (!seeneFolder.exists()) {
    		seeneFolder.mkdirs();
    		successTexture = Helper.downloadFile(sO.getPosterURL(), seeneFolder);
    		successModel = Helper.downloadFile(sO.getModelURL(), seeneFolder);
    	} else {
    		log(seeneFolder.getAbsolutePath() + " already exists!", LogLevel.info);
    	}
    	
    	if ((successModel) && (successTexture)) {
    		log("Seene: " + seeneFolder.getAbsolutePath() + " downloaded", LogLevel.info);
    		Helper.createFolderIcon(seeneFolder);
    		return true;
    	}
    	return false;
    }
    
    

	@Override
	public void run() {
		
		// first run. User should select where to store the Seenes.
		if(!configFile.exists()) {
			// Show dialog for directory browsing
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int fileChooserReturnValue = chooser.showDialog(null,"select directory to store your backuped seenes");
	        
	        // Check if chosen or canceled
	        if(fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
	            log("user selected path: " + chooser.getSelectedFile().getPath(),LogLevel.info);
	            // Write path in configuration file
	            PrintWriter writer;
				try {
					writer = new PrintWriter(configFile);
					writer.println("storage=" + chooser.getSelectedFile().getPath());
	    			writer.close();
	    			log("new configuration file " + configFile.getPath() + " written!",LogLevel.info);
	    			
	        		showSettingsDialog();
	        		
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //try/catch
	        } else {
	        	log("user canceled selection!",LogLevel.info);
	        	System.exit(0);
	        } // if ... JFileChooser.APPROVE_OPTION
		} else {
			readConfiguration(configFile);
   		} 

	    // GUI stuff
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
        
        //Region Panels and Splits
        JSplitPane allSplits = createSplitPanels();
        mainFrame.add(allSplits);
        
        // Region West-North: select the seene pool (public/private/local)
        btPoolPublicSeenes.addActionListener(this);
        btPoolPrivateSeenes.addActionListener(this);
        btPoolLocalSeenes.addActionListener(this);
        ButtonGroup poolButtonGroup = new ButtonGroup();
        poolButtonGroup.add(btPoolPublicSeenes);
        poolButtonGroup.add(btPoolPrivateSeenes);
        poolButtonGroup.add(btPoolLocalSeenes);
        panelWestNorth.add(btPoolPublicSeenes);
        panelWestNorth.add(btPoolPrivateSeenes);
        panelWestNorth.add(btPoolLocalSeenes);
        
        // Region West-South: displays the seenes in a pool
        panelWestSouth.setBackground(Color.white);
        
        // Region East-South: Log output window
        logOutput.setLineWrap(true);
        // embed logOutput in BorderLayout
        panelEastSouth.setLayout(new BorderLayout());
        panelEastSouth.add(logOutputScrollPane, BorderLayout.CENTER);
        panelEastSouth.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));


		mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        
	}
	
	// create all JSplitPanes for the GUI
	private JSplitPane createSplitPanels() {
		
		JSplitPane splitWestNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitWestNorthSouth.setTopComponent(panelWestNorth);
		splitWestNorthSouth.setBottomComponent(scrollWestSouth);
		splitWestNorthSouth.setDividerSize(2);
		splitWestNorthSouth.setDividerLocation(35);
		splitWestNorthSouth.setResizeWeight(0.5);
		
		Dimension minimumSize = new Dimension(250, 500);
		splitWestNorthSouth.setMinimumSize(minimumSize);
		
		JSplitPane splitEastNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitEastNorthSouth.setTopComponent(panelEastNorth);
		splitEastNorthSouth.setBottomComponent(panelEastSouth);
		splitEastNorthSouth.setDividerSize(2);
		splitEastNorthSouth.setDividerLocation(768-250);
		splitEastNorthSouth.setResizeWeight(0.5);
		
		JSplitPane splitWestEast = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitWestEast.setLeftComponent(splitWestNorthSouth);
		splitWestEast.setRightComponent(splitEastNorthSouth);
		splitWestEast.setOneTouchExpandable(true);
		splitWestEast.setDividerSize(2);
		splitWestEast.setDividerLocation(250);
		splitWestEast.setResizeWeight(0.5);
		
		return splitWestEast;
	}
	
	// the action listener for the GUI
	public void actionPerformed(ActionEvent arg0) {
		
		if(arg0.getSource() == this.taskBackupPublic) {
			doTaskBackupPublicSeenes(storage.getPublicDir(), 
					new GUIReporter());
		} else if(arg0.getSource() == this.taskBackupPrivate) {
			doTaskBackupPrivateSeenes(storage.getPrivateDir(),
					new GUIReporter());
		} else if(arg0.getSource() == this.testDoLogin) {
	    	doTestLogin();
	    } else if (arg0.getSource() == this.btPoolPublicSeenes) {
	    	parsePool(storage.getPublicDir());
	    } else if (arg0.getSource() == this.btPoolPrivateSeenes) {
	    	parsePool(storage.getPrivateDir());	    	
	    } else if (arg0.getSource() == this.btPoolLocalSeenes) {
	    	parsePool(storage.getOfflineDir());	    	
	    }
	}
	
	private void parsePool(File baseDir) {
		File[] files = baseDir.listFiles();
		Arrays.sort(files);
		if (files != null) {
			panelWestSouth.removeAll();
			panelWestSouth.repaint();
			panelWestSouth.setLayout(new WrapLayout());  
		    for (int i = files.length - 1; i > -1; i--) {
		      System.out.print(files[i].getAbsolutePath());
		      if (files[i].isDirectory()) {
			       System.out.print(" (folder)\n");
			       ImageIcon imgDir = new ImageIcon(files[i].getAbsolutePath() + File.separator + "folder.png");
			       JLabel newLabel = new JLabel();
			       newLabel = new JLabel (files[i].getName().substring(0, 20), imgDir, JLabel.LEFT);
			       newLabel.setToolTipText(files[i].getAbsolutePath());
			       newLabel.setHorizontalTextPosition(JLabel.CENTER);
			       newLabel.setVerticalTextPosition(JLabel.BOTTOM);
			       newLabel.setHorizontalAlignment(SwingConstants.LEFT);
			       newLabel.addMouseListener(this);
			       panelWestSouth.add(newLabel);
		      } // directory
		    } // for
		    mainFrame.setVisible(true);
		 } // !=null
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
		    			log("new configuration file " + configFile.getPath() + " written!",LogLevel.info);
		    			
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
	
	// returns the value of a certain parameter from the configuration file
	private static String getParameterFromConfiguration(File cf, String param) {
		String paramEq = new String(param + "=");
		if(cf.exists() && !cf.isDirectory()) {
    		log("looking for " + paramEq + " in " + cf.getAbsolutePath() ,LogLevel.info);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(cf));
				String line;
    			while ((line = br.readLine()) != null) {
    				if (line.substring(0, paramEq.length()).equalsIgnoreCase(paramEq)) {
    					log("configured " + paramEq + " is: " + "<secret, shh!>" /*line.substring(paramEq.length())*/,LogLevel.debug);
    					br.close();
    					return line.substring(paramEq.length());
    				}
    			}
    			br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} // if (f.exists()...
    	return null;
	}
	
	
	// reads the complete configuration file into globals
	private boolean readConfiguration(File cf) {
    	if(cf.exists() && !cf.isDirectory()) {
    		log("reading application configuration...",LogLevel.debug);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(cf));
				String line;
    			while ((line = br.readLine()) != null) {
    				if (line.substring(0, 7).equalsIgnoreCase("api_id=")) {
    					log("configured api_id is: " + line.substring(7),LogLevel.debug);
    					seeneAPIid = line.substring(7);
    				}
    				if (line.substring(0, 8).equalsIgnoreCase("storage=")) {
    					log("configured starage path: " + line.substring(8),LogLevel.debug);
    					storage.setPath(line.substring(8));
    					storageOK = storage.initializer();
    				}
    				if (line.substring(0, 9).equalsIgnoreCase("username=")) {
    					log("configured username: " + line.substring(9),LogLevel.debug);
    					seeneUser = line.substring(9);
    				}
    				if (line.substring(0, 11).equalsIgnoreCase("passphrase=")) {
    					log("configured passphrase: " + line.substring(11),LogLevel.debug);
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
	
	public static void log(String logLine, String LogLevelKey) {
		if (APPLICATION_LOG_MODE.contains(LogLevelKey)) {
			logOutput.append(LogLevel.getLogLevelText(LogLevelKey) + ": " + logLine + "\n");
			logOutput.setCaretPosition(logOutput.getDocument().getLength());
			System.out.println(LogLevel.getLogLevelText(LogLevelKey) + ": " + logLine);
			//TODO: writing to logfile
		}
	}

}

