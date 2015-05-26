package org.seeneclub.toolkit;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
	
	static Boolean commandLineUsed = false;
	static String programVersion = "0.1b"; 
	static JFrame mainFrame = new JFrame("...::: Seene-Club-Toolkit-GUI v." + programVersion + " :::...");
	
	// We need a local storage for the Seenes
	SeeneStorage storage = new SeeneStorage();
  	Boolean storageOK = false;
  	
  	// GUI-Panels
	//JPanel panelWest = new JPanel();		//Navigation Panel
	JPanel panelWestNorth = new JPanel();	//Seene Pool Selection 
	JPanel panelWestSouth = new JPanel();	//Seene Folders View
	// Seene listview goes to panelWestSouth and that should be scrollable!
	JScrollPane scrollWestSouth = new JScrollPane (panelWestSouth, 
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel panelEastNorth = new JPanel();
	static JPanel panelProgressbar = new JPanel();
	JPanel panelLogOutput = new JPanel();
	
	
	// Elements for Panel WestNorth (seene pool selection)
	JToggleButton btPoolPublicSeenes = new JToggleButton("public");
	JToggleButton btPoolPrivateSeenes = new JToggleButton("private");
	JToggleButton btPoolOtherSeenes = new JToggleButton("other");
	JToggleButton btPoolLocalSeenes = new JToggleButton("local");
	
	// Elements for Region EastSouth (progressbar and log output)
    static JTextArea logOutput = new JTextArea();
    static JProgressBar progressbar = new JProgressBar();
    JScrollPane logOutputScrollPane = new JScrollPane(logOutput);
    
    // Elements for Region EastNorth (Seene Display)
    JToolBar toolbar = new JToolBar();
    JButton tbSaveLocal = new JButton("save local");
    ModelGraphics modelDisplay = new ModelGraphics();
	
	// Settings Dialog
	static File configDir = null;
    static File configFile = null;
	JDialog settingsDialog = new JDialog();
    static String seeneUser = new String();
    static String seenePass = new String();
    static String seeneAPIid = new String();
	
	// Task Menu Items
    JMenuItem taskBackupPublic = new JMenuItem("retrieve my public seenes");
    JMenuItem taskBackupPrivate = new JMenuItem("retrieve my private seenes");
    JMenuItem taskBackupOther = new JMenuItem("retrieve someone else's seenes");
    JMenuItem taskBackupByURL = new JMenuItem("retrieve public seene by URL");
    
    // Tests Menu Items
    JMenuItem testDoLogin = new JMenuItem("Test Login");
    
    // method main - all begins with a thread!
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		
		configDir = new File(System.getProperty("user.home") + File.separator + ".seene-club");
		if (configDir.exists() || configDir.mkdirs()) {
			configFile = new File(configDir + File.separator + "configuration");
		}
		
		
		// command line parsing (using Apache commons CLI)
		CommandLineParser parser = new GnuParser();
		
		Options options = new Options();
		
		Option backupOption = OptionBuilder.withLongOpt("backup")
										   .withDescription("backup public or private Seenes from Server")
										   .hasArg()
										   .withArgName("VISIBILITY")
										   .create("b");
		
		Option countOption = OptionBuilder.withLongOpt("count")
				   						  .withDescription("number of seenes to retrieve")
				   						  .hasArg()
				   						  .withArgName("COUNT")
				   						  .create("c");
		
		Option userOption   = OptionBuilder.withLongOpt("username")
										   .withDescription("Seene Username")
				   						   .hasArg()
				   						   .withArgName("USERNAME")
				   						   .create("u");
		
		Option passOption   = OptionBuilder.withLongOpt("password")
					 		 			   .withDescription("Seene Password")
					 		 			   .hasArg()
					 					   .withArgName("PASSWORD")
					 					   .create("p");
		
		Option targetOption = OptionBuilder.withLongOpt("output-target")
				 						   .withDescription("target directory for the backup")
				 						   .hasArg()
				 						   .withArgName("PATH")
				 						   .create("o");
		
		options.addOption(backupOption);
		options.addOption(countOption);
		options.addOption(userOption);
		options.addOption(passOption);
		options.addOption(targetOption);
		
		try {
		    // parse the command line arguments
		    CommandLine line = parser.parse( options, args );

		    if (line.hasOption("backup")) {
		    	commandLineUsed = true;
		    	int count;
		    	// handle public backup. No Login is required.
		    	if (line.getOptionValue("backup").equalsIgnoreCase("public")) {
		    		if (line.hasOption("username")) {
		    			seeneUser = line.getOptionValue("username");
		    		} else {
		    			String errorText = new String("for public backup the Seene username is required!");
		    			throw new org.apache.commons.cli.ParseException(errorText);
		    		}
		    		
		    		if (line.hasOption("count")) {
			    		count = Integer.parseInt(line.getOptionValue("count"));
			    	} else {
			    		count = 10;
			    		log("retrieving the " + count + " last public seenes.",LogLevel.info);
			    		log("if you want to download more use the --count option!",LogLevel.info);
			    	}
		    		
		    		String targetDir = line.hasOption("output-target")
		    			? line.getOptionValue("output-target")
		    			: System.getProperty("user.dir"); // do the backup to current working dir, if no output-target is given.
		    		doTaskBackupPublicSeenes(new File(targetDir),count);
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
	    				
	    				if (line.hasOption("count")) {
				    		count = Integer.parseInt(line.getOptionValue("count"));
				    	} else {
				    		count = 10;
				    		log("retrieving the " + count + " last private seenes.",LogLevel.info);
				    		log("if you want to download more use the --count option!",LogLevel.info);
				    	}
	    				
			    		String targetDir = line.hasOption("output-target")
				    			? line.getOptionValue("output-target")
				    			: System.getProperty("user.dir"); // do the backup to current working dir, if no output-target is given.
				    			doTaskBackupPrivateSeenes(new File(targetDir),count);
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
			log(e.getMessage(),LogLevel.error);
			
		}
	}	
    
    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }
    
 
    
    // example: java -jar seene-club-toolkit.jar -b public -c 100 -u paf
    private static void doTaskBackupPublicSeenes(File targetDir, int last) {
    	try {
    		log("Public Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    	
    		log("Resolving name to id", LogLevel.info);
			String userId = SeeneAPI.usernameToId(seeneUser);
			log("Seene user: " + userId, LogLevel.debug);

			log("Getting index of last " + last + " public seenes", LogLevel.info);
			List<SeeneObject> index = SeeneAPI.getPublicSeenes(userId, last);
			log("You have at least " + index.size() + " public seenes", LogLevel.info);
			
			downloadInThreads(index, targetDir, 4);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void doTaskBackupOtherSeenes(File targetDir, String username, int count) {
    	try {
    		log("Others Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    	
    		log("Resolving name to id", LogLevel.info);
			String userId = SeeneAPI.usernameToId(username);
			log("Seene user: " + userId, LogLevel.debug);

			log("Getting index of " + username + "'s last " + count + " public seenes", LogLevel.info);
			List<SeeneObject> index = SeeneAPI.getPublicSeenes(userId, count);
			log(username + " has at least " + index.size() + " public seenes", LogLevel.info);
			
			downloadInThreads(index, targetDir, 4);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void doTaskBackupByURL(File targetDir, String surl) {
    	try {
    		log("Seene will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    		
    		List<SeeneObject> index = SeeneAPI.getPublicSeeneByURL(surl);
    		downloadInThreads(index, targetDir, 1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    // example: java -jar seene-club-toolkit.jar -b private -c 500 -u paf -o /home/paf/myPrivateSeenes
    private static void doTaskBackupPrivateSeenes(File targetDir, int last) {
    	try {
        	log("Private Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    	
        	log("Resolving name to id", LogLevel.info);
			String userId = SeeneAPI.usernameToId(seeneUser);
			log("Seene user: " + userId, LogLevel.debug);

			Token token;
			{
				// @TODO Mathias, we may cache the token someday
				log("Logging in",LogLevel.info);
				token = SeeneAPI.login(seeneAPIid, seeneUser, seenePass);
			}
					
			log("Getting index of last " + last + " private seenes",LogLevel.info);
			List<SeeneObject> index = SeeneAPI.getPrivateSeenes(token, userId, last);
			log("You have at least " + index.size() + " private seenes", LogLevel.info);
			
			downloadInThreads(index, targetDir, 4);
			
		} catch (Exception e) {
			StringBuffer inform = new StringBuffer("The following error occured:\n");
			inform.append(e.getMessage());
			inform.append("\n\nPlease check your Seene credentials configuration and your internet connection!");
            log(e.getMessage(),LogLevel.error);
            JOptionPane.showMessageDialog(null,  inform.toString(), "Backup Error", JOptionPane.ERROR_MESSAGE);
		}
    }
    
    
    // Downloading with more than one thread is faster for most internet connections
    private static void downloadInThreads(List<SeeneObject> seenesToDownload, File targetDir, int number_of_threads) {
    	try {
	    	List<SeeneObject> toDownloadIndex = new ArrayList<SeeneObject>();
			
			// First we check which Seenes are already in the target-directory
			for(SeeneObject sO : seenesToDownload) {
				String folderName = SeeneStorage.generateSeeneFolderName(sO,seeneUser);
				File seeneFolder = new File(targetDir.getAbsolutePath() + File.separator + folderName);
				if (!seeneFolder.exists()) {
					toDownloadIndex.add(sO);
				}
			}
			
			if (toDownloadIndex.size()==0) {
				log("All of them are already backupped. NOTHING TO DO!", LogLevel.info);
			} else {
			
				if (!commandLineUsed) {
					progressbar.setValue(0);
					progressbar.setMaximum(toDownloadIndex.size());
				}
				
				log(toDownloadIndex.size() + " of them are not backupped.", LogLevel.info);
			
				List<Thread> threads = new ArrayList<Thread>();
				
				int tc = 0; // thread count
			
				for(SeeneObject o : toDownloadIndex) {
					//downloadSeene(o,targetDir);
					SeeneDownloader sd = new SeeneDownloader(o, targetDir, seeneUser);
					SeeneDownloadCompleteListener l = new SeeneDownloadCompleteListener() {
						
						@Override
						public void notifyOfThreadComplete(SeeneDownloader seeneDownloader) {
							//log("DOWNLOAD COMPLETE: " + seeneDownloader.getSeeneObject().getCaption(),LogLevel.info);
							//Thread t = getThreadByName(seeneDownloader.getSeeneObject().getShortCode());
							if (!commandLineUsed) {
								progressbar.setValue(progressbar.getValue()+1);
							}
						}
					};
					
					sd.addListener(l);
	
					Thread t = new Thread(sd);
					t.setName(sd.getSeeneObject().getShortCode());
					t.start();
					log("FETCHING Seene " + o.getShortCode() + " " + o.getCaption(),LogLevel.info);
					threads.add(t);
					tc++;
					// start up to "number_of_threads" DL Threads at once
					if (tc == number_of_threads) {
						for (Thread tj : threads) {
							tj.join();
						}
						tc = 0;
						threads.clear();
					}
				}
				// finally join all unjoined threads
				for (Thread t : threads) {
					t.join();
				}
	
				log("ALL DOWNLOADS COMPLETE!",LogLevel.info);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
        taskBackupOther.addActionListener(this);
        taskBackupByURL.addActionListener(this);
        
        taskMenu.add(taskBackupPublic);
        taskMenu.add(taskBackupPrivate);
        taskMenu.add(taskBackupOther);
        taskMenu.add(taskBackupByURL);
        
        JMenu testMenu = new JMenu("Tests");
        testDoLogin.addActionListener(this);
        
        testMenu.add(testDoLogin);
                
        bar.add(fileMenu);
        bar.add(taskMenu);
        //bar.add(testMenu);
        
        mainFrame.setJMenuBar(bar);
        
        //Region Panels and Splits
        JSplitPane allSplits = createSplitPanels();
        mainFrame.add(allSplits);
        
        // Region West-North: select the seene pool (public/private/local)
        btPoolPublicSeenes.addActionListener(this);
        btPoolPrivateSeenes.addActionListener(this);
        btPoolOtherSeenes.addActionListener(this);
        btPoolLocalSeenes.addActionListener(this);
        ButtonGroup poolButtonGroup = new ButtonGroup();
        poolButtonGroup.add(btPoolPublicSeenes);
        poolButtonGroup.add(btPoolPrivateSeenes);
        poolButtonGroup.add(btPoolOtherSeenes);
        poolButtonGroup.add(btPoolLocalSeenes);
        panelWestNorth.add(btPoolPublicSeenes);
        panelWestNorth.add(btPoolPrivateSeenes);
        panelWestNorth.add(btPoolOtherSeenes);
        panelWestNorth.add(btPoolLocalSeenes);
        
        // Region West-South: displays the seenes in a pool
        panelWestSouth.setBackground(Color.white);
        
        // Region East-North: Seene display
        toolbar.add(tbSaveLocal);
        tbSaveLocal.setEnabled(false);
        
        //panelEastNorth.add(toolbar);
        
		panelEastNorth.add(modelDisplay);
        
                
        // Region East-South: Log output window
        logOutput.setLineWrap(true);
        // embed logOutput in BorderLayout
        panelProgressbar.setLayout(new BorderLayout());
        progressbar.setMinimum(0);
        progressbar.setMaximum(100);
        progressbar.setValue(0);
        progressbar.setStringPainted(true);
        panelProgressbar.add(progressbar,BorderLayout.CENTER);
        
        panelLogOutput.setLayout(new BorderLayout());
        panelLogOutput.add(logOutputScrollPane, BorderLayout.CENTER);
        panelLogOutput.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

		mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        
	}
	
	// create all JSplitPanes for the GUI
	private JSplitPane createSplitPanels() {
		
		// Split the Pool Selection and the Seene Folders View
		JSplitPane splitWestNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitWestNorthSouth.setTopComponent(panelWestNorth);
		splitWestNorthSouth.setBottomComponent(scrollWestSouth);
		splitWestNorthSouth.setDividerSize(2);
		splitWestNorthSouth.setDividerLocation(35);
		splitWestNorthSouth.setResizeWeight(0.5);
		
		Dimension minimumSize = new Dimension(250, 500);
		splitWestNorthSouth.setMinimumSize(minimumSize);
		
		JSplitPane splitEastSouthPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitEastSouthPanel.setTopComponent(panelLogOutput);
		splitEastSouthPanel.setBottomComponent(panelProgressbar);
		splitEastSouthPanel.setDividerSize(0);
		splitEastSouthPanel.setDividerLocation(170);
		splitEastSouthPanel.setResizeWeight(0.5);
		
		JSplitPane splitEastNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitEastNorthSouth.setTopComponent(panelEastNorth);
		splitEastNorthSouth.setBottomComponent(splitEastSouthPanel);
		splitEastNorthSouth.setDividerSize(2);
		splitEastNorthSouth.setDividerLocation(768-250);
		splitEastNorthSouth.setResizeWeight(0.5);
		
		// Split Navigation Area and Display Area
		JSplitPane splitWestEast = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitWestEast.setLeftComponent(splitWestNorthSouth);
		splitWestEast.setRightComponent(splitEastNorthSouth);
		splitWestEast.setOneTouchExpandable(true);
		splitWestEast.setDividerSize(2);
		splitWestEast.setDividerLocation(300);
		splitWestEast.setResizeWeight(0.5);
		
		return splitWestEast;
	}
	
	// the action listener for the GUI
	public void actionPerformed(ActionEvent arg0) {
		
		if(arg0.getSource() == this.taskBackupPublic) {
			try {
				int cnt = Integer.parseInt((String)JOptionPane.showInputDialog(mainFrame, "How many of your last public\nseenes do you want to retrieve?",
						"Enter the number of seenes", JOptionPane.PLAIN_MESSAGE, null, null, null));
				if (cnt > 0) {
					Thread dlThread = new Thread() {
						public void run() {
							doTaskBackupPublicSeenes(storage.getPublicDir(),cnt);		
						}
					};
					dlThread.start();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,  "Invalid number entered. Aborting!", "Backup Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if(arg0.getSource() == this.taskBackupPrivate) {
			try {
				int cnt = Integer.parseInt((String)JOptionPane.showInputDialog(mainFrame, "How many of your last private\nseenes do you want to retrieve?",
						"Enter the number of seenes", JOptionPane.PLAIN_MESSAGE, null, null, null));
				if (cnt > 0) {
					Thread dlThread = new Thread() {
						public void run() {
							doTaskBackupPrivateSeenes(storage.getPrivateDir(),cnt);		
						}
					};
					dlThread.start();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,  "Invalid number entered. Aborting!", "Backup Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if(arg0.getSource() == this.taskBackupOther) {
			String uid = (String)JOptionPane.showInputDialog(mainFrame, "Whose public seenes do you want to retrieve?",
                    "Retrieving someone else's seenes", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if ((uid != null) && (uid.length() > 0)) {
				try {
					int cnt = Integer.parseInt((String)JOptionPane.showInputDialog(mainFrame, "How many of " + uid + "'s last\nseenes do you want to retrieve?",
							"Enter the number of seenes", JOptionPane.PLAIN_MESSAGE, null, null, null));
					if (cnt > 0) {
						Thread dlThread = new Thread() {
							public void run() {
								doTaskBackupOtherSeenes(storage.getOthersDir(),uid,cnt);		
							}
						};
						dlThread.start();
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,  "Invalid number entered. Aborting!", "Backup Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if(arg0.getSource() == this.taskBackupByURL) {
			String surl = (String)JOptionPane.showInputDialog(mainFrame, "Enter the URL of the seene to download:\n(example: http://seene.co/s/GX3bug)",
                    "Retrieving a seene by URL", JOptionPane.PLAIN_MESSAGE, null, null, "http://seene.co/s/");
			if ((surl != null) && (surl.length() > 0)) {
				Thread dlThread = new Thread() {
					public void run() {
						doTaskBackupByURL(storage.getOthersDir(),surl);		
					}
				};
				dlThread.start();
			}
		} else if(arg0.getSource() == this.testDoLogin) {
	    	doTestLogin();
	    } else if (arg0.getSource() == this.btPoolPublicSeenes) {
	    	parsePool(storage.getPublicDir());
	    } else if (arg0.getSource() == this.btPoolPrivateSeenes) {
	    	parsePool(storage.getPrivateDir());	    	
	    } else if (arg0.getSource() == this.btPoolOtherSeenes) {
	    	parsePool(storage.getOthersDir());	
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
		      log(files[i].getAbsolutePath(),LogLevel.debug);
		      if (files[i].isDirectory()) {
			       ImageIcon imgDir = new ImageIcon(files[i].getAbsolutePath() + File.separator + "folder.png");
			       JLabel newLabel = new JLabel();
			       if (files[i].getName().length() >= 19) {
			    	   newLabel = new JLabel (files[i].getName().substring(0, 19), imgDir, JLabel.LEFT);
				       newLabel.setToolTipText(files[i].getAbsolutePath());
				       newLabel.setHorizontalTextPosition(JLabel.CENTER);
				       newLabel.setVerticalTextPosition(JLabel.BOTTOM);
				       newLabel.setHorizontalAlignment(SwingConstants.LEFT);
				       newLabel.addMouseListener(this);
				       panelWestSouth.add(newLabel);
			       }
		      } // directory
		    } // for
		    mainFrame.setVisible(true);
		 } // !=null
	}
	

	@Override
	public void mouseClicked(MouseEvent mevent) {
		// *** single click handling ***
		if (mevent.getClickCount() == 1) {
			// clear markup state of all file labels
			clearFileLabels(panelWestSouth);
			// read element which received the event
			JLabel labelReference=(JLabel)mevent.getSource();
			labelReference.setOpaque(true);
			labelReference.setBackground(Color.decode("0xDDDDEE"));
		}
		// *** double click handling ***		
		if (mevent.getClickCount() == 2) {
			JLabel labelReference=(JLabel)mevent.getSource();
			String seeneFilePath=labelReference.getToolTipText();
			if (seeneFilePath != null) { 
			      File seeneFolder = new File(seeneFilePath);
			      if(seeneFolder.exists()) {
			    	  if (seeneFolder.isDirectory()) {
			    		  openSeene(seeneFolder);
			    	  } // isDirectory
			      } // exists
			} // not null
		} // double click
		
	}
	
	private void openSeene(File seeneFolder) {
		SeeneObject s = new SeeneObject(seeneFolder);
		// load the model-data from file system
		log("Loading Seene Model: " +  s.getModelFile().getAbsolutePath(),LogLevel.info);
		s.getModel().loadModelDataFromFile();
		log("Model width: " +  s.getModel().getDepthWidth(),LogLevel.info);
		log("Model height: " +  s.getModel().getDepthHeight() ,LogLevel.info);
		
		modelDisplay.setModel(s.getModel());
		
		//mainFrame.setVisible(true);
		
	}
	
	
	@SuppressWarnings("serial")
	class ModelGraphics extends Canvas {
		
		public SeeneModel model; 
		public int pointSize=2;
		public boolean inverted=true;
		
		public ModelGraphics(){
			setSize(240*getPointSize(), 240*getPointSize());	
	        setBackground(Color.white);
	        addMouseListener(new MouseAdapter(){
				public void mousePressed(MouseEvent e){
					if (model!=null) {
						int w = model.getDepthWidth();
						float max = model.getMaxFloat();
						int mx = w - e.getX() / pointSize - 1;
						int my = e.getY() / pointSize;
						int n = mx * w + my;
						float f = model.getFloats().get(n);
						float cf = floatGreyScale(f, max);
						System.out.println(mx +  " - " + my + " - float number: " + n + " - float value: " + f + " - color: " + cf);
					}
				}
			});
	    }

		public ModelGraphics(SeeneModel seeneModel){
			setModel(seeneModel);
	        setSize(model.getDepthWidth(), model.getDepthHeight()); 
	        setBackground(Color.white);
	    }
		
	    public void paint(Graphics g){

	    	if (model!=null) {
	        
		        int c=0;
		        float f;
		        float max = model.getMaxFloat();
		        int w = model.getDepthWidth();
		        int h = model.getDepthHeight();
		        int p = getPointSize();
		        float cf;
		        
		        for (int x=0;x<w;x++) {
		        	for (int y=0;y<h;y++) {
		        		f = model.getFloats().get(c);

		        		cf = floatGreyScale(f, max);
		        		Color newColor = new Color(cf,cf,cf);
		        		g.setColor(newColor);
		        		
		        		g.fillRect((w-x-1)*p, y*p , p, p);
		        		
		        		//System.out.println("c: " + c + " - x: " + x + " - y: " + y + " - f:" + f + " - color: " + cf);
		        		
		        		c++;
		        	} // for y
		        } //  for x
	    	} // if (model!=null)
	    }
	    
	    private float floatGreyScale(float value, float maximum) {
	    	if (inverted) return value/maximum;
	    	return 1 - value/maximum;
	    }
	    
	    
	    private void repaintGraphics() {
	    	if (model!=null) {
				Graphics g = getGraphics();
				setSize(model.getDepthWidth()*getPointSize(), model.getDepthHeight()*getPointSize());
				this.paint(g);
			}
	    }
	    
	    public SeeneModel getModel() {
			return model;
		}

		public void setModel(SeeneModel model) {
			this.model = model;
			repaintGraphics();
			
		}
		
		public int getPointSize() {
			return pointSize;
		}

		public void setPointSize(int pointSize) {
			this.pointSize = pointSize;
			repaintGraphics();
		}
	}

	// unmark file labels. called when other file gets selected  
	private void clearFileLabels(Container container) {
	    for (Component c : container.getComponents()) {
	        if (c instanceof JLabel) {
	           ((JLabel)c).setBackground(Color.white);
	        } else
	        if (c instanceof Container) {
	        	clearFileLabels((Container)c);
	        }
	    }
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
    		log("reading application configuration: " + cf.getAbsolutePath(),LogLevel.debug);
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
			if (!commandLineUsed) {
				logOutput.append(LogLevel.getLogLevelText(LogLevelKey) + ": " + logLine + "\n");
				logOutput.setCaretPosition(logOutput.getDocument().getLength());
			}
			System.out.println(LogLevel.getLogLevelText(LogLevelKey) + ": " + logLine);
			//TODO: writing to logfile
		}
	}



}

