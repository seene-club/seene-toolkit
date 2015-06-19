package org.seeneclub.toolkit;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
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
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
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
	JPanel mainViewPanel = new JPanel();
	static JPanel panelProgressbar = new JPanel();
	JPanel panelLogOutput = new JPanel();
	
	// Elements for Panel WestNorth (seene pool selection)
	JToggleButton btPoolPublicSeenes = new JToggleButton("public");
	JToggleButton btPoolPrivateSeenes = new JToggleButton("private");
	JToggleButton btPoolOtherSeenes = new JToggleButton("other");
	JToggleButton btPoolLocalSeenes = new JToggleButton("local");
	
	// Elements for Panel WestSouth (seenes in a pool list)
	JPopupMenu rClickPopup = new JPopupMenu("Menu");
		
	// Elements for Region EastSouth (progressbar and log output)
    static JTextArea logOutput = new JTextArea();
    static JProgressBar progressbar = new JProgressBar();
    JScrollPane logOutputScrollPane = new JScrollPane(logOutput);
    
    // Elements for Region EastNorth (Seene Display)
    JPanel mainToolbarPanel = new JPanel();
    JToolBar toolbar = new JToolBar(BorderLayout.PAGE_START);
    JButton tbSaveLocal = new JButton("save local");
    JButton tbShowModel = new JButton("show model");
    JButton tbShowPoster = new JButton("show poster");
    JButton tbUploadSeene = new JButton("upload");
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
    
    // Seene Object we are currently working on
    SeeneObject currentSeene = null;
    JLabel currentSelection = null;
    SeeneNormalizer normalizer = new SeeneNormalizer();
    
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
    
    private static void doUploadSeene(File uploadsLocalDir,SeeneObject sO) {
    	try {
			Token token = SeeneAPI.login(seeneAPIid, seeneUser, seenePass);
			SeeneAPI.uploadSeene(uploadsLocalDir, sO, seeneUser, token);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		// disabling lightweight rendering for PopUps and Tooltips (otherwise they are behind the canvas)
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
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
        itemSettings.setIcon(Helper.iconFromImageResource("settings.png", 16));
        itemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showSettingsDialog();
            }
        });
        
        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.setIcon(Helper.iconFromImageResource("exit.png", 16));
        itemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });
        
        fileMenu.add(itemSettings);
        fileMenu.add(itemExit);
        
        JMenu taskMenu = new JMenu("Tasks");
        
        taskBackupPublic.setIcon(Helper.iconFromImageResource("download.png", 16));
        taskBackupPrivate.setIcon(Helper.iconFromImageResource("downloadp.png", 16));
        taskBackupOther.setIcon(Helper.iconFromImageResource("downloado.png", 16));
        taskBackupByURL.setIcon(Helper.iconFromImageResource("downloadu.png", 16));
        
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
        
        JMenu twoXtwoSubmenu = new JMenu("add to 2x2 grid");
        twoXtwoSubmenu.setIcon(Helper.iconFromImageResource("2x2grid.png", 16));
        JMenuItem twXtwUpperLeft = new JMenuItem("place upper left");
        twXtwUpperLeft.setIcon(Helper.iconFromImageResource("2x2gridul.png", 16));
        JMenuItem twXtwUpperRight = new JMenuItem("place upper right");
        twXtwUpperRight.setIcon(Helper.iconFromImageResource("2x2gridur.png", 16));
        JMenuItem twXtwBottomLeft = new JMenuItem("place bottom left");
        twXtwBottomLeft.setIcon(Helper.iconFromImageResource("2x2gridbl.png", 16));
        JMenuItem twXtwBottomRight = new JMenuItem("place bottom right");
        twXtwBottomRight.setIcon(Helper.iconFromImageResource("2x2gridbr.png", 16));
        
        JMenu threeXthreeSubmenu = new JMenu("add to 3x3 grid");
        threeXthreeSubmenu.setIcon(Helper.iconFromImageResource("3x3grid.png", 16));
        JMenuItem tXtUpperLeft = new JMenuItem("place upper left");
        tXtUpperLeft.setIcon(Helper.iconFromImageResource("3x3gridul.png", 16));
        JMenuItem tXtUpperCenter = new JMenuItem("place upper center");
        tXtUpperCenter.setIcon(Helper.iconFromImageResource("3x3griduc.png", 16));
        JMenuItem tXtUpperRight = new JMenuItem("place upper right");
        tXtUpperRight.setIcon(Helper.iconFromImageResource("3x3gridur.png", 16));
        JMenuItem tXtMiddleLeft = new JMenuItem("place middle left");
        tXtMiddleLeft.setIcon(Helper.iconFromImageResource("3x3gridml.png", 16));
        JMenuItem tXtMiddleCenter = new JMenuItem("place middle center");
        tXtMiddleCenter.setIcon(Helper.iconFromImageResource("3x3gridmc.png", 16));
        JMenuItem tXtMiddleRight = new JMenuItem("place middle right");
        tXtMiddleRight.setIcon(Helper.iconFromImageResource("3x3gridmr.png", 16));
        JMenuItem tXtBottomLeft = new JMenuItem("place bottom left");
        tXtBottomLeft.setIcon(Helper.iconFromImageResource("3x3gridbl.png", 16));
        JMenuItem tXtBottomCenter = new JMenuItem("place bottom center");
        tXtBottomCenter.setIcon(Helper.iconFromImageResource("3x3gridbc.png", 16));
        JMenuItem tXtBottomRight = new JMenuItem("place bottom right");
        tXtBottomRight.setIcon(Helper.iconFromImageResource("3x3gridbr.png", 16));
        
        // Other Menu Items for right click on Seene folder
        JMenuItem miShowInFS = new JMenuItem("show in file explorer");
        miShowInFS.setIcon(Helper.iconFromImageResource("show.png", 16));
        JMenuItem miDeleteFS = new JMenuItem("delete from local file system");
        miDeleteFS.setIcon(Helper.iconFromImageResource("delete.png", 16));
        
        ActionListener popupListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              if(event.getSource() == twXtwUpperLeft) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,3);
             	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == twXtwUpperRight) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,1);
             	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == twXtwBottomLeft) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,4);
             	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == twXtwBottomRight) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,2);
             	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtUpperLeft) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,7);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtUpperCenter) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,4);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtUpperRight) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,1);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtMiddleLeft) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,8);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtMiddleCenter) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,5);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtMiddleRight) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,2);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtBottomLeft) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,9);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtBottomCenter) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,6);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == tXtBottomRight) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,3);
            	  modelDisplay.setSeeneObject(currentSeene);
              }
              if(event.getSource() == miShowInFS) {
            	  try {
            		  Desktop.getDesktop().open(new File(currentSelection.getToolTipText()));
            	  } catch (IOException e) { e.printStackTrace(); }
              }
              if(event.getSource() == miDeleteFS) {
            	  int an = JOptionPane.showConfirmDialog(mainFrame,
            			    "Do you really want to delete\n"
            			    + currentSelection.getToolTipText()+ "\n"
                    		+ "from your computer?",
            			    "Delete Seene?",
            			    JOptionPane.YES_NO_OPTION);
            	  if (an == JOptionPane.YES_OPTION) {
            		  Helper.deleteDirectory(new File(currentSelection.getToolTipText()));
            		  if (btPoolPublicSeenes.getModel().isSelected())	parsePool(storage.getPublicDir());
            	      if (btPoolPrivateSeenes.getModel().isSelected())	parsePool(storage.getPrivateDir());
            	      if (btPoolOtherSeenes.getModel().isSelected())	parsePool(storage.getOthersDir());
            		  if (btPoolLocalSeenes.getModel().isSelected()) 	parsePool(storage.getOfflineDir()); 
            	 }
              }
            }
          };
          
        twXtwUpperLeft.addActionListener(popupListener);
        twXtwUpperRight.addActionListener(popupListener);
        twXtwBottomLeft.addActionListener(popupListener);
        twXtwBottomRight.addActionListener(popupListener);
        
        tXtUpperLeft.addActionListener(popupListener);
        tXtUpperCenter.addActionListener(popupListener);
        tXtUpperRight.addActionListener(popupListener);
        tXtMiddleLeft.addActionListener(popupListener);
        tXtMiddleCenter.addActionListener(popupListener);
        tXtMiddleRight.addActionListener(popupListener);
        tXtBottomLeft.addActionListener(popupListener);
        tXtBottomCenter.addActionListener(popupListener);
        tXtBottomRight.addActionListener(popupListener);
        
        twoXtwoSubmenu.add(twXtwUpperLeft);
        twoXtwoSubmenu.add(twXtwUpperRight);
        twoXtwoSubmenu.add(twXtwBottomLeft);
        twoXtwoSubmenu.add(twXtwBottomRight);
        
        threeXthreeSubmenu.add(tXtUpperLeft);
        threeXthreeSubmenu.add(tXtUpperCenter);
        threeXthreeSubmenu.add(tXtUpperRight);
        threeXthreeSubmenu.add(tXtMiddleLeft);
        threeXthreeSubmenu.add(tXtMiddleCenter);
        threeXthreeSubmenu.add(tXtMiddleRight);
        threeXthreeSubmenu.add(tXtBottomLeft);
        threeXthreeSubmenu.add(tXtBottomCenter);
        threeXthreeSubmenu.add(tXtBottomRight);
        
        miShowInFS.addActionListener(popupListener);
        miDeleteFS.addActionListener(popupListener);
        
        rClickPopup.add(twoXtwoSubmenu);
        rClickPopup.add(threeXthreeSubmenu);
        rClickPopup.add(miShowInFS);
        rClickPopup.add(miDeleteFS);
        
        
        // Region East-North: Seene display & Toolbar
        ActionListener toolbarListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              if(event.getSource() == tbSaveLocal) {
            	  String localname = (String)JOptionPane.showInputDialog(mainFrame, "Give your Seene a name:",
                          "Saving as local Seene", JOptionPane.PLAIN_MESSAGE, null, null, currentSeene.getLocalname());
      			
            	  if ((localname != null) && (localname.length() > 0)) {
            		  File savePath = new File(storage.getOfflineDir().getAbsolutePath() + File.separator + localname);
            		  savePath.mkdirs();
            		  File mFile = new File(savePath.getAbsoluteFile() + File.separator + "scene.oemodel");
            		  File pFile = new File(savePath.getAbsoluteFile() + File.separator + "poster.jpg");
            		  currentSeene.setLocalname(localname);
            		  currentSeene.getModel().saveModelDateToFile(mFile);
            		  currentSeene.getPoster().saveTextureToFile(pFile);
            		  Helper.createFolderIcon(savePath, null);
            		  parsePool(storage.getOfflineDir());
            		  btPoolLocalSeenes.getModel().setSelected(true);
            	  } else {
            		  JOptionPane.showMessageDialog(null,  "Aborted. Seene not saved!", "Seene not saved.", JOptionPane.ERROR_MESSAGE);
            	  }
              }
              if(event.getSource() == tbShowModel) {
            	  modelDisplay.repaintModelOnly();
              }
              if(event.getSource() == tbShowPoster) {
            	  modelDisplay.repaintPosterOnly();
              }
              if(event.getSource() == tbUploadSeene) {
            	 doUploadSeene(storage.getUploadsDir() ,currentSeene);
              }
            }
        };
        
        tbSaveLocal.addActionListener(toolbarListener);
        tbShowModel.addActionListener(toolbarListener);
        tbShowPoster.addActionListener(toolbarListener);
        tbUploadSeene.addActionListener(toolbarListener);
        
        toolbar.add(tbSaveLocal);
        toolbar.add(tbShowModel);
        toolbar.add(tbShowPoster);
        toolbar.add(tbUploadSeene);
        
        mainToolbarPanel.setLayout(new BorderLayout());
        mainToolbarPanel.add(toolbar,BorderLayout.PAGE_START);
        
        currentSeene = new SeeneObject();
        
		mainViewPanel.add(modelDisplay);
        
                
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
	
	public SeeneObject inlaySeene(SeeneObject sO, String inlayPath, int divisor, int position) {
		File inlayFile = new File(inlayPath);
		SeeneObject inlayObject = new SeeneObject(inlayFile);
		
		// load the inlay model data and the poster image from file system
		log("Loading Inlay Model: " +  inlayObject.getModelFile().getAbsolutePath(),LogLevel.info);
		inlayObject.getModel().loadModelDataFromFile();
		SeeneModel inlayModel = inlayObject.getModel();
		log("Inlay Model width: " +  inlayModel.getDepthWidth(),LogLevel.info);
		log("Inlay Model height: " +  inlayModel.getDepthHeight() ,LogLevel.info);
		
		
		//Just testing here ...
		//inlayModel = normalizer.normalizeModelToFarthest(inlayModel);
		//inlayModel = normalizer.normalizeModelToClosest(inlayModel);
		
		
		log("Loading Inlay Poster: " +  inlayObject.getPosterFile().getAbsolutePath(),LogLevel.info);
		inlayObject.getPoster().loadTextureFromFile();
		SeeneTexture inlayPoster = inlayObject.getPoster();
		
		sO.setModel(inlaySeeneModel(sO.getModel(), inlayModel, divisor, position));
	    sO.setPoster(inlaySeenePoster(sO.getPoster(), inlayPoster, divisor, position));
		return sO;
	}
	
	private SeeneTexture inlaySeenePoster(SeeneTexture originPoster, SeeneTexture inlayPoster, int divisor, int position) {
		
		Image originImage = originPoster.getTextureImage();
		Image inlayImage = inlayPoster.getTextureImage();
		
		int ow = originImage.getWidth(null);
		int oh = originImage.getHeight(null);
		int new_iw = ow / divisor;
		int new_ih = oh / divisor;
		
		int new_ix = (position - 1) % divisor * new_iw;
		int new_iy = (int) Math.floor((position - 1) / divisor) * new_ih;
		
		BufferedImage originBuffered = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_ARGB); 

	    Graphics2D tGr = originBuffered.createGraphics();
	    tGr.drawImage(originImage, 0, 0, ow, oh, null);
	    tGr.drawImage(inlayImage, new_ix, new_iy, new_iw, new_ih, null);
	    tGr.dispose();
	    
	    originPoster.setTextureImage(originBuffered);

		return originPoster;
	}


	private SeeneModel inlaySeeneModel(SeeneModel originModel, SeeneModel inlayModel, int divisor, int position) {
	
		int ow = originModel.getDepthWidth();
		int oh = originModel.getDepthHeight();
		int iw = inlayModel.getDepthWidth();
		int ih = inlayModel.getDepthHeight();
		
		int boundary = ow / divisor;
		float ev = (float) iw / boundary;
		float inlay_limit = (iw * ih) / ev;
		int partial_size = (ow * oh) / (divisor * divisor);
		
		// calculate insert position
		int w_pos = (int)(((position - 1)  % divisor) * boundary + (float)Math.floor((position - 1) / divisor) * divisor * partial_size);
		float r_pos = 0;
		int z = 0;
				
		float f;
		for (int i=0;i<inlay_limit;i++) {
			// copy floats
			f = inlayModel.getFloats().get(Math.round(r_pos));
			originModel.getFloats().set(w_pos,f);
			
			// adjust read and write positions after reading
			r_pos += ev;
			w_pos++;
			
			// adjust read and write positions beyond boundaries
			if ((w_pos>boundary-1) && (w_pos%boundary==0))  {
				z++;
				if (z%ev!=0) {
					i=i+iw;
					r_pos=r_pos+(iw*ev);
				}
				w_pos = w_pos + (ow - boundary);
			}
		}
		
		return findModelExtema(originModel);
	}
	
	public SeeneModel findModelExtema(SeeneModel mO) {
		float max = mO.getMaxFloat();
		float min = mO.getMinFloat();
		int floatCount = mO.getDepthWidth() * mO.getDepthHeight();
		float f;
		for(int i = 0; i<floatCount;i++) {
			f = mO.getFloats().get(i);
			if (f > max) max = f;
			if (f < min) min = f;
		}
		log("new extrema: min: " + min + " - max: " + max,LogLevel.debug);
		mO.setMinFloat(min);
		mO.setMaxFloat(max);
		return mO;
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
		
		JSplitPane splitMainViewPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitMainViewPanel.setTopComponent(mainToolbarPanel);
		splitMainViewPanel.setBottomComponent(mainViewPanel);
		splitMainViewPanel.setDividerSize(2);
		splitMainViewPanel.setDividerLocation(35);
		splitMainViewPanel.setResizeWeight(0.5);
		
		JSplitPane splitEastNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitEastNorthSouth.setTopComponent(splitMainViewPanel);
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
							parsePool(storage.getPublicDir());
		            		btPoolPublicSeenes.getModel().setSelected(true);
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
							parsePool(storage.getPrivateDir());
		            		btPoolPrivateSeenes.getModel().setSelected(true);
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
								parsePool(storage.getOthersDir());
			            		btPoolOtherSeenes.getModel().setSelected(true);
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
		      if ((files[i].isDirectory()) && (!files[i].getName().startsWith("."))) {
			       ImageIcon imgDir = new ImageIcon(files[i].getAbsolutePath() + File.separator + "folder.png");
			       String labelname;
			       if (files[i].getName().length() >= 19) {
			    	   labelname = files[i].getName().substring(0, 19);
			       } else {
			           labelname = files[i].getName();
			       }
		    	   JLabel newLabel = new JLabel (labelname, imgDir, JLabel.LEFT);
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
	public void mouseClicked(MouseEvent mevent) {
		// *** single click handling ***
		if (mevent.getClickCount() == 1) {
			currentSelection=(JLabel)mevent.getSource();
			if(SwingUtilities.isRightMouseButton(mevent)){
		        rClickPopup.show((Component)mevent.getSource(), mevent.getX(), mevent.getY());
		    }
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
		currentSeene = new SeeneObject(seeneFolder);
		// load the model-data from file system
		log("Loading Seene Model: " +  currentSeene.getModelFile().getAbsolutePath(),LogLevel.info);
		currentSeene.getModel().loadModelDataFromFile();
		normalizer.setNormMaxFloat(currentSeene.getModel().getMaxFloat());
		normalizer.setNormMinFloat(currentSeene.getModel().getMinFloat());
		currentSeene.getPoster().loadTextureFromFile();
		log("Model width: " +  currentSeene.getModel().getDepthWidth(),LogLevel.info);
		log("Model height: " +  currentSeene.getModel().getDepthHeight() ,LogLevel.info);
		
		modelDisplay.setModel(currentSeene.getModel());
		modelDisplay.setPoster(currentSeene.getPoster());
		
		//mainFrame.setVisible(true);
		
	}
	
	
	@SuppressWarnings("serial")
	class ModelGraphics extends Canvas {
		
		public SeeneObject seeneObject;
		public SeeneModel model; 
		public SeeneTexture poster;

		public int canvasSize=240;
		public int pointSize=2;
		public boolean inverted=true;
		
		public ModelGraphics(){
			setSize(canvasSize*getPointSize(), canvasSize*getPointSize());	
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
						log(mx +  " - " + my + " - float number: " + n + " - float value: " + f + " - color: " + cf,LogLevel.debug);
					}
				}
			});
	    }

		/*
		public ModelGraphics(SeeneModel seeneModel){
			setModel(seeneModel);
	        setSize(model.getDepthWidth(), model.getDepthHeight()); 
	        setBackground(Color.white);
	    } */
		
	    public void paint(Graphics g){

	    	paintModel(g,model);
	    	paintPoster(g, poster);
	    	
	    }
	    
	    private void paintPoster(Graphics g, SeeneTexture poster) {
	    	if (poster!=null) {
	    		Image texture = poster.getTextureImage();
	    		int new_width = canvasSize*getPointSize();
	    		int new_height = canvasSize*getPointSize();
	    		BufferedImage textureTransformed = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_ARGB); 

			    Graphics2D tGr = textureTransformed.createGraphics();
			    tGr.rotate(Math.toRadians(90), new_width/2, new_height/2);
			    tGr.drawImage(texture, 0, 0, new_height, new_width, null);
			    tGr.dispose();
			    
	    		g.drawImage(textureTransformed, 0, 0, null);
	    	}
	    }
	    
	    private void paintModel(Graphics g,SeeneModel model) {
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
	    	if ((model!=null) && (poster!=null)) {
				Graphics g = getGraphics();
				setSize(model.getDepthWidth()*getPointSize(), model.getDepthHeight()*getPointSize());
				this.paint(g);
			}
	    }
	    
	    public void repaintModelOnly() {
	    	if (model!=null) {
				Graphics g = getGraphics();
				setSize(model.getDepthWidth()*getPointSize(), model.getDepthHeight()*getPointSize());
				this.paintModel(g, model);
			}
	    }
	    
	    public void repaintPosterOnly() {
	    	if (poster!=null) {
				Graphics g = getGraphics();
				this.paintPoster(g, poster);
			}
	    }
	    
	    // Getter and Setter
	    public SeeneObject getSeeneObject() {
			return seeneObject;
		}
		public void setSeeneObject(SeeneObject seeneObject) {
			this.seeneObject = seeneObject;
			this.model = seeneObject.getModel();
			this.poster = seeneObject.getPoster();
			repaintGraphics();
		}
	    public SeeneModel getModel() {
			return model;
		}
		public void setModel(SeeneModel model) {
			this.model = model;
			repaintModelOnly();
		}
		public int getPointSize() {
			return pointSize;
		}
		public void setPointSize(int pointSize) {
			this.pointSize = pointSize;
			repaintGraphics();
		}
		public SeeneTexture getPoster() {
			return poster;
		}
		public void setPoster(SeeneTexture poster) {
			this.poster = poster;
			repaintModelOnly();
		}
		public boolean isInverted() {
			return inverted;
		}
		public void setInverted(boolean inverted) {
			this.inverted = inverted;
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
    	settingsDialog.setLocationRelativeTo(mainFrame);
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
    					log("configured storage path: " + line.substring(8),LogLevel.debug);
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

