package org.seeneclub.toolkit;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.vanitasvitae.depthmapneedle.JPEG;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.android.camera.util.XmpUtil;

public class SeeneToolkit implements Runnable, ActionListener, MouseListener {
	
	public static final String APPLICATION_LOG_MODE = //LogLevel.debug + 
													  LogLevel.info +
													  LogLevel.warn +
													  LogLevel.error +
													  LogLevel.fatal;
	
	static Boolean commandLineUsed = false;
	static String programVersion = "0.4b"; 
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
    static String seeneUser = new String("");
    static String seenePass = new String("");
    static String seeneAPIid = new String("");
    static String seeneAuthorizationCode = new String("");
    static String seeneBearerToken = new String("");
    static ProxyData pd = new ProxyData(null, 0);
    
    // Authorization Dialog
    JDialog authorizationDialog = new JDialog();
	
	// Task Menu Items
    JMenuItem taskBackupPublic = new JMenuItem("retrieve my public seenes");
    JMenuItem taskBackupPrivate = new JMenuItem("retrieve my private seenes");
    JMenuItem taskBackupOther = new JMenuItem("retrieve someone else's seenes");
    JMenuItem taskBackupByURL = new JMenuItem("retrieve public seene by URL");
    
    // Mask Menu Items
    JMenuItem maskAll = new JMenuItem("mask all");
    JMenuItem maskRemove = new JMenuItem("remove mask");
    JMenuItem maskInvert = new JMenuItem("invert mask");
    JMenuItem maskSetDepth = new JMenuItem("set depth for masked area");
    JMenuItem maskDivideByTwo = new JMenuItem("devide depth in masked area by 2");
    JMenuItem maskDivideByThree = new JMenuItem("devide depth in masked area by 3");
        
    // Tests Menu Items
    JMenuItem testSomething = new JMenuItem("Test Something");
    
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
		
		Option uploadOption = OptionBuilder.withLongOpt("upload")
										   .withDescription("upload to your private Seenes")
										   .create("u");
		
		Option countOption = OptionBuilder.withLongOpt("count")
				   						  .withDescription("number of seenes to retrieve")
				   						  .hasArg()
				   						  .withArgName("COUNT")
				   						  .create("c");
		
		Option userOption   = OptionBuilder.withLongOpt("username")
										   .withDescription("Seene Username")
				   						   .hasArg()
				   						   .withArgName("USERNAME")
				   						   .create("uid");
		
		Option passOption   = OptionBuilder.withLongOpt("password")
					 		 			   .withDescription("Seene Password")
					 		 			   .hasArg()
					 					   .withArgName("PASSWORD")
					 					   .create("pwd");
		
		Option targetOption = OptionBuilder.withLongOpt("output-target")
				 						   .withDescription("target directory for the backup")
				 						   .hasArg()
				 						   .withArgName("PATH")
				 						   .create("o");
		
		options.addOption(backupOption);
		options.addOption(uploadOption);
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
		    			getCredentialsForConsole(line);
	    				
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
		    } // END OF BACKUP Options
		    // Handle UPLOAD
		    else if (line.hasOption("upload")) {
		    	commandLineUsed = true;
		    	// first we check if there's a model and poster file in the current working directory
		    	String wd = System.getProperty("user.dir");
		    	File mF = new File(wd + File.separator + STK.SEENE_MODEL);
		    	File pF = new File(wd + File.separator + STK.SEENE_TEXTURE);
		    	if (mF.exists()) {
		    		if (pF.exists()) {
		    			// username has to be set for upload
				    	if (line.hasOption("username")) {
			    			getCredentialsForConsole(line);
			    			// preparing directories
			    			File sourceDir = new File(wd);
			    			File tempUlDir = new File(wd + File.separator + ".~upload~temp");
			    			tempUlDir.mkdir();
			    			// loading seene
			    			SeeneObject ulSeene = new SeeneObject(sourceDir);
			    			ulSeene.getModel().loadModelDataFromFile();
			    			ulSeene.getPoster().loadTextureFromFile();
			    			ulSeene.setCaption("uploaded with https://github.com/seene-club/seene-toolkit");
			    			// uploading seene
			    			doUploadSeeneOldMethod(tempUlDir, ulSeene);
			    			// removing temp
			    			Helper.deleteDirectory(tempUlDir);
				    	} else {
			    			String errorText = new String("to upload a Seene your credentials are required!");
			    			throw new org.apache.commons.cli.ParseException(errorText);
			    		} // if (line.hasOption("username"))
		    		} else { 
		    			log("no texture file to upload!\nmissing " + STK.SEENE_TEXTURE,LogLevel.error);
		    		} // if (pF.exists()) 
		    	} else {
		    		log("no model file to upload!\nmissing " + STK.SEENE_MODEL,LogLevel.error);
		    	} // if (mF.exists()) 
		    } // if (line.hasOption("upload"))
		} catch( org.apache.commons.cli.ParseException exp ) {
			commandLineUsed = true;
		    log("parameter exception: " + exp.getMessage() , LogLevel.error);
		}

		// Start GUI only if NO command line argument is used!
		if (!commandLineUsed) new Thread(new SeeneToolkit()).start();
	}


	private static void getCredentialsForConsole(CommandLine line) {
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
	}
	

    
    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }
    
 
    
    // example: java -jar seene-club-toolkit.jar -b public -c 100 -uid paf
    private static void doTaskBackupPublicSeenes(File targetDir, int last) {
    	try {
    		log("Public Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);

    		SeeneAPI myAPI = new SeeneAPI(pd);
    	
    		log("Resolving name to id", LogLevel.info);
			String userId = myAPI.usernameToId(seeneUser);
			log("Seene user: " + userId, LogLevel.debug);

			log("Getting index of last " + last + " public seenes", LogLevel.info);
			List<SeeneObject> index = myAPI.getPublicSeenes(userId, last);
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
    		
    		SeeneAPI myAPI = new SeeneAPI(pd);
    	
    		log("Resolving name to id", LogLevel.info);
			String userId = myAPI.usernameToId(username);
			log("Seene user: " + userId, LogLevel.debug);

			log("Getting index of " + username + "'s last " + count + " public seenes", LogLevel.info);
			List<SeeneObject> index = myAPI.getPublicSeenes(userId, count);
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
    		
    		SeeneAPI myAPI = new SeeneAPI(pd);
    		
    		List<SeeneObject> index = myAPI.getPublicSeeneByURL(surl);
    		downloadInThreads(index, targetDir, 1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    // example: java -jar seene-club-toolkit.jar -b private -c 500 -uid paf -o /home/paf/myPrivateSeenes
    private static void doTaskBackupPrivateSeenes(File targetDir, int last) {
    	try {
        	log("Private Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
        	
        	SeeneAPI myAPI = new SeeneAPI(pd);
    	
        	log("Resolving name to id", LogLevel.info);
			String userId = myAPI.usernameToId(seeneUser);
			log("Seene user: " + userId, LogLevel.debug);

			Token token;
			{
				// @TODO Mathias, we may cache the token someday
				log("Logging in",LogLevel.info);
				token = myAPI.login(seeneAPIid, seeneUser, seenePass);
			}
					
			log("Getting index of last " + last + " private seenes",LogLevel.info);
			List<SeeneObject> index = myAPI.getPrivateSeenes(token, userId, last);
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
    
    private void doUploadSeene(SeeneObject sO) {
    	SeeneAPI myAPI = new SeeneAPI(pd);
    	try {
			myAPI.uploadSeene(sO, getValidBearerToken(), storage.getUploadsDir());
		} catch (Exception e) {
			log("UPLOAD FAILED: " + e.getMessage(),LogLevel.error);
		}
    }
    
    private static void doUploadSeeneOldMethod(File uploadsLocalDir,SeeneObject sO) {
    	try {
    		SeeneAPI myAPI = new SeeneAPI(pd);
			Token token = myAPI.login(seeneAPIid, seeneUser, seenePass);
			myAPI.uploadSeeneOldMethod(uploadsLocalDir, sO, seeneUser, token);
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
					SeeneDownloader sd = new SeeneDownloader(o, targetDir, seeneUser, pd);
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
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		// first run. User should select where to store the Seenes.
		if(!configFile.exists()) {
			// Show dialog for directory browsing
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int fileChooserReturnValue = chooser.showDialog(null,"Select directory to store your Seenes");
	        
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
	        		showAuthorizationDialog();
	        		
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
        
        JMenu maskMenu = new JMenu("Mask");
        
        maskAll.setIcon(Helper.iconFromImageResource("maskall.png", 16));
        maskRemove.setIcon(Helper.iconFromImageResource("masknothing.png", 16));
        maskInvert.setIcon(Helper.iconFromImageResource("maskinvert.png", 16));
        maskSetDepth.setIcon(Helper.iconFromImageResource("masksetvalue.png", 16));
        maskDivideByTwo.setIcon(Helper.iconFromImageResource("maskdividedby2.png", 16));
        maskDivideByThree.setIcon(Helper.iconFromImageResource("maskdividedby3.png", 16));
        
        maskAll.addActionListener(this);
        maskRemove.addActionListener(this);
        maskInvert.addActionListener(this);
        maskSetDepth.addActionListener(this);
        maskDivideByTwo.addActionListener(this);
        maskDivideByThree.addActionListener(this);
        
        maskMenu.add(maskAll);
        maskMenu.add(maskRemove);
        maskMenu.add(maskInvert);
        maskMenu.addSeparator();
        maskMenu.add(maskSetDepth);
        maskMenu.addSeparator();
        maskMenu.add(maskDivideByTwo);
        maskMenu.add(maskDivideByThree);
        
        JMenu testMenu = new JMenu("Tests");
        testSomething.addActionListener(this);
        
        testMenu.add(testSomething);
                
        bar.add(fileMenu);
        bar.add(taskMenu);
        bar.add(maskMenu);
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
        
        btPoolPublicSeenes.setPreferredSize(new Dimension(55,22));
        btPoolPrivateSeenes.setPreferredSize(new Dimension(55,22));
        btPoolOtherSeenes.setPreferredSize(new Dimension(55,22));
        btPoolLocalSeenes.setPreferredSize(new Dimension(55,22));
        
        btPoolPublicSeenes.setMargin(new Insets(0, 0, 0, 0));
        btPoolPrivateSeenes.setMargin(new Insets(0, 0, 0, 0));
        btPoolOtherSeenes.setMargin(new Insets(0, 0, 0, 0));
        btPoolLocalSeenes.setMargin(new Insets(0, 0, 0, 0));
        
        btPoolPublicSeenes.setFont(new Font("Arial", Font.PLAIN, 10));
        btPoolPrivateSeenes.setFont(new Font("Arial", Font.PLAIN, 10));
        btPoolOtherSeenes.setFont(new Font("Arial", Font.PLAIN, 10));
        btPoolLocalSeenes.setFont(new Font("Arial", Font.PLAIN, 10));
        
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
        JMenuItem miGenerateXMP = new JMenuItem("generate XMP (JPG with depthmap)");
        JMenuItem miLoadTextureFromPoster = new JMenuItem("load texture from Seene " + STK.SEENE_TEXTURE);
        JMenuItem miLoadTextureFromPosterOriginal = new JMenuItem("load texture from " + STK.XMP_ORIGINAL_JPG);
        JMenuItem miLoadTextureFromXMPCombined = new JMenuItem("load texture from " + STK.XMP_COMBINED_JPG);
        JMenuItem miLoadModelFromOEmodel = new JMenuItem("load model from " + STK.SEENE_MODEL);
        JMenuItem miLoadModelFromPNG = new JMenuItem("load model from " + STK.XMP_DEPTH_PNG);
        JMenuItem miLoadModelFromXMPCombined = new JMenuItem("load model from " + STK.XMP_COMBINED_JPG);
        JMenuItem miShowInFS = new JMenuItem("show in file explorer");
        JMenuItem miDeleteFS = new JMenuItem("delete from local file system");
        miGenerateXMP.setIcon(Helper.iconFromImageResource("xmp.png", 16));
        miLoadTextureFromPoster.setIcon(Helper.iconFromImageResource("texture.png", 16));
        miLoadTextureFromPosterOriginal.setIcon(Helper.iconFromImageResource("texture.png", 16));
        miLoadTextureFromXMPCombined.setIcon(Helper.iconFromImageResource("texture.png", 16));
        miLoadModelFromOEmodel.setIcon(Helper.iconFromImageResource("model.png", 16));
        miLoadModelFromPNG.setIcon(Helper.iconFromImageResource("model.png", 16));
        miLoadModelFromXMPCombined.setIcon(Helper.iconFromImageResource("model.png", 16));
        miShowInFS.setIcon(Helper.iconFromImageResource("show.png", 16));
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
              if(event.getSource() == miGenerateXMP) {
            	  generateXMP(currentSelection.getToolTipText());
              }
              if(event.getSource() == miLoadTextureFromPoster) {
            	  currentSeene.setPoster(loadProprietaryPoster(currentSelection.getToolTipText()));
            	  modelDisplay.setPoster(currentSeene.getPoster());
            	  modelDisplay.repaintPosterOnly();
              }
              if(event.getSource() == miLoadTextureFromPosterOriginal) {
            	  currentSeene.setPoster(loadXMPOriginalPoster(currentSelection.getToolTipText()));
            	  modelDisplay.setPoster(currentSeene.getPoster());
            	  modelDisplay.repaintPosterOnly();
              }
              if(event.getSource() == miLoadTextureFromXMPCombined) {
            	  currentSeene.setPoster(loadXMPCombinedPoster(currentSelection.getToolTipText()));
            	  modelDisplay.setPoster(currentSeene.getPoster());
            	  modelDisplay.repaintPosterOnly();
              }
              if(event.getSource() == miLoadModelFromOEmodel) {
            	 currentSeene.setModel(loadProprietaryModel(currentSelection.getToolTipText()));
            	 modelDisplay.setModel(currentSeene.getModel());
            	 modelDisplay.repaintModelOnly();
              }
              if(event.getSource() == miLoadModelFromPNG) {
            	  currentSeene.setModel(loadXMPDepthPNGModel(currentSelection.getToolTipText()));
            	  modelDisplay.setModel(currentSeene.getModel());
             	  modelDisplay.repaintModelOnly();
              }
              if(event.getSource() == miLoadModelFromXMPCombined) {
            	  currentSeene.setModel(loadXMPCombinedModel(currentSelection.getToolTipText()));
            	  modelDisplay.setModel(currentSeene.getModel());
             	  modelDisplay.repaintModelOnly();
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
        
        miGenerateXMP.addActionListener(popupListener);
        miLoadTextureFromPoster.addActionListener(popupListener);
        miLoadTextureFromPosterOriginal.addActionListener(popupListener);
        miLoadTextureFromXMPCombined.addActionListener(popupListener);
        miLoadModelFromOEmodel.addActionListener(popupListener);
        miLoadModelFromPNG.addActionListener(popupListener);
        miLoadModelFromXMPCombined.addActionListener(popupListener);
        miShowInFS.addActionListener(popupListener);
        miDeleteFS.addActionListener(popupListener);
        
        rClickPopup.add(twoXtwoSubmenu);
        rClickPopup.add(threeXthreeSubmenu);
        rClickPopup.addSeparator();
        rClickPopup.add(miGenerateXMP,STK.POPUP_POSITION_GENERATE_XMP);
        rClickPopup.addSeparator();
        rClickPopup.add(miLoadTextureFromPoster,STK.POPUP_POSITION_LOAD_TEXTURE_POSTER);
        rClickPopup.add(miLoadTextureFromPosterOriginal,STK.POPUP_POSITION_LOAD_TEXTURE_POSTER_ORIGINAL);
        rClickPopup.add(miLoadTextureFromXMPCombined,STK.POPUP_POSITION_LOAD_TEXTURE_XMP);
        rClickPopup.add(miLoadModelFromOEmodel,STK.POPUP_POSITION_LOAD_MODEL_OEMODEL);
        rClickPopup.add(miLoadModelFromPNG,STK.POPUP_POSITION_LOAD_MODEL_PNG);
        rClickPopup.add(miLoadModelFromXMPCombined,STK.POPUP_POSITION_LOAD_MODEL_XMP);
        rClickPopup.addSeparator();
        rClickPopup.add(miShowInFS);
        rClickPopup.add(miDeleteFS);
        
        // Region East-North: Seene display & Toolbar
        ActionListener toolbarListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              if (event.getSource() == tbSaveLocal) {
            	  if (modelDisplay.getModel()!=null) {
	            	  String localname = (String)JOptionPane.showInputDialog(mainFrame, "Give your Seene a name:",
	                          "Saving as local Seene", JOptionPane.PLAIN_MESSAGE, null, null, currentSeene.getLocalname());
	      			
	            	  if ((localname != null) && (localname.length() > 0)) {
	            		  File savePath = new File(storage.getOfflineDir().getAbsolutePath() + File.separator + localname);
	            		  savePath.mkdirs();
	            		  // Save proprietary Seene files
	            		  File mFile = new File(savePath.getAbsoluteFile() + File.separator + STK.SEENE_MODEL);
	            		  File tFile = new File(savePath.getAbsoluteFile() + File.separator + STK.SEENE_TEXTURE);
	            		  
	            		  currentSeene.setLocalname(localname);
	            		  currentSeene.getModel().saveModelDataToFile(mFile);
	            		  currentSeene.getPoster().saveTextureToFile(tFile);
	            		  
	            		  // Save XMP components
	            		  generateXMP(savePath.getAbsoluteFile().toString());
	            		     
	            		  Helper.createFolderIcon(savePath, null);
	            		  parsePool(storage.getOfflineDir());
	            		  btPoolLocalSeenes.getModel().setSelected(true);
	            		
	            	  } else {
	            		  JOptionPane.showMessageDialog(mainFrame,  "Aborted. Seene not saved!", "Seene not saved.", JOptionPane.ERROR_MESSAGE);
	            	  }
            	  } else { showNoSeeneThereDialog("Can't save!"); }
            		  
              }
              if(event.getSource() == tbShowModel) {
            	  if (modelDisplay.getModel()!=null) {
            		  modelDisplay.repaintModelOnly();
            	  } else { showNoSeeneThereDialog("Can't show model!"); }
              }
              if(event.getSource() == tbShowPoster) {
            	  if (modelDisplay.getModel()!=null) {
            		  modelDisplay.repaintPosterOnly();
            	  } else { showNoSeeneThereDialog("Can't show poster!"); }
              }
              if(event.getSource() == tbUploadSeene) {
            	  if (modelDisplay.getModel()!=null) {
            		  showUploadDialog();
            	  } else { showNoSeeneThereDialog("Can't upload!"); }
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
	
	public static SeeneModel loadXMPDepthPNGModel(String seenePath) {
		File xFile = new File(seenePath + File.separator + STK.XMP_COMBINED_JPG);
		File pFile = new File(seenePath + File.separator + STK.XMP_DEPTH_PNG);
		SeeneModel mO = new SeeneModel();
		mO.loadModelDataFromPNG(pFile, xFile);
		return mO;
	}

	public static SeeneModel loadXMPCombinedModel(String seenePath) {
		File xFile = new File(seenePath + File.separator + STK.XMP_COMBINED_JPG);
		String xmpFilepath = xFile.getAbsolutePath();
		SeeneModel mO = new SeeneModel();
		
		JPEG image = new JPEG(xmpFilepath);
		// Trying to extract the depthmap from the XMP enhanced JPG
		if (image.exportDepthMap()) {
			log("Depthmap extracted for file " + xmpFilepath, LogLevel.info);
			String depthmapFilePath = xmpFilepath.substring(0, xmpFilepath.length()-8) + "_depth.png";
			mO.loadModelDataFromPNG(new File(depthmapFilePath), xFile);
		} else log("There is no Depthmap in file " + xmpFilepath, LogLevel.warn);
		
		return mO;
	}


	public static SeeneTexture loadXMPCombinedPoster(String seenePath) {
		File xFile = new File(seenePath + File.separator + STK.XMP_COMBINED_JPG);
		return loadVerticalOrientedPoster(xFile);
	}
	
	public static SeeneTexture loadXMPOriginalPoster(String seenePath) {
		File oFile = new File(seenePath + File.separator + STK.XMP_ORIGINAL_JPG);
		return loadVerticalOrientedPoster(oFile);
	}


	private static SeeneTexture loadVerticalOrientedPoster(File vopFile) {
		BufferedImage textureImage = null;
		SeeneTexture tO = null;
		
		try {
			textureImage = ImageIO.read(vopFile);
			tO = new SeeneTexture(Helper.rotateAndResizeImage(textureImage, textureImage.getWidth(), textureImage.getHeight(), 270));
		} catch (IOException e) {
			log("Could not open: " + vopFile.getAbsolutePath(),LogLevel.error);
		}
		 
		return tO;
	}

	public static SeeneModel loadProprietaryModel(String seenePath) {
		File mFile = new File(seenePath + File.separator + STK.SEENE_MODEL);
		SeeneModel mO = new SeeneModel(mFile);
		mO.loadModelDataFromFile();
		return mO;
	}

	public static SeeneTexture loadProprietaryPoster(String seenePath) {
		File tFile = new File(seenePath + File.separator + STK.SEENE_TEXTURE);
		SeeneTexture tO = new SeeneTexture(tFile);
		tO.loadTextureFromFile();
		return tO;
	}
	
	public static void generateXMP(String seenePath, float far) {
		generateXMP(seenePath, far, STK.CALCULATION_METHOD_STK_PRESERVE);
	}
	
	public static void generateXMP(String seenePath) {
		// Load proprietary Seene Model for MaxDepth 
		SeeneModel mO = loadProprietaryModel(seenePath);
		generateXMP(seenePath, mO.getMaxDepth());
	}
	
	public static void generateXMP(String seenePath, int calculationMethod) {
		// Load proprietary Seene Model for MaxDepth 
		SeeneModel mO = loadProprietaryModel(seenePath);
		generateXMP(seenePath, mO.getMaxDepth(), calculationMethod);
	}
	
	public static void generateXMP(String seenePath, float far, int calculationMethod) {
		// Load proprietary Seene Files 
		SeeneModel mO = loadProprietaryModel(seenePath);
		SeeneTexture tO = loadProprietaryPoster(seenePath);
		
		// Save XMP components from proprietary Seene
		File pFile = new File(seenePath + File.separator + STK.XMP_DEPTH_PNG);
		File oFile = new File(seenePath + File.separator + STK.XMP_ORIGINAL_JPG);
		File xFile = new File(seenePath + File.separator + STK.XMP_COMBINED_JPG);
		
		tO.saveTextureRotatedToFile(oFile, 90);
		tO.saveTextureRotatedToFile(xFile, 90);
		mO.saveModelDataToPNGwithFar(pFile, far, calculationMethod);
		
		injectDepthmapXMP(mO, pFile, xFile, far);
	}


	private static void injectDepthmapXMP(SeeneModel mO, File pFile, File xFile, float far) {
		// Inject XMP Metadata into the XMP JPG
		JPEG image = new JPEG(xFile.getAbsolutePath());
		byte[] depthmap = image.getDepthMap(pFile.getAbsolutePath());
		byte[] depthmap_base64 = image.base64_decode(depthmap);
		  
		XMPMeta xmpMeta = XmpUtil.extractOrCreateXMPMeta(xFile.getAbsolutePath());
		  
		try {
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Mime", "image/png");
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Format", "RangeInverse");
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Far", far);
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Near", mO.getMinDepth());
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:ImageWidth", STK.WORK_WIDTH);
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:ImageHeight", STK.WORK_HEIGHT);
			xmpMeta.setProperty(XmpUtil.GOOGLE_DEPTH_NAMESPACE, "GDepth:Data", depthmap_base64);
			
			XmpUtil.writeXMPMeta(xFile.getAbsolutePath(), xmpMeta);	
		} catch (XMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void showNoSeeneThereDialog(String title) {
		JOptionPane.showMessageDialog(mainFrame,  "There's no Seene in the editor!\n" + 
				"Please double click a Seene from the list on the left side.\n\n" + 
				"No, Seenes there?\n" +
				"Please use the 'Tasks' menu to retrieve some!" , title, JOptionPane.WARNING_MESSAGE);
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
		inlayModel = normalizer.normalizeModelToClosest(inlayModel);
		
		
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
		float max = -1;
		float min = 1000;
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
		splitWestEast.setDividerLocation(250);
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
						parsePool(storage.getOthersDir());
	            		btPoolOtherSeenes.getModel().setSelected(true);
					}
				};
				dlThread.start();
			}
		 } /* TEST Menu */ 
	      else if(arg0.getSource() == this.testSomething) {
	    	 try {
				log("bearer token: " + getValidBearerToken(),LogLevel.info);
			} catch (Exception e) {
				e.printStackTrace();
			}	
	    } /* POOL Selection */
	      else if (arg0.getSource() == this.btPoolPublicSeenes) {
	    	parsePool(storage.getPublicDir());
	    } else if (arg0.getSource() == this.btPoolPrivateSeenes) {
	    	parsePool(storage.getPrivateDir());	    	
	    } else if (arg0.getSource() == this.btPoolOtherSeenes) {
	    	parsePool(storage.getOthersDir());	
	    } else if (arg0.getSource() == this.btPoolLocalSeenes) {
	    	parsePool(storage.getOfflineDir());	    	
	    } /* MASK Menu */ 
	      else if(arg0.getSource() == this.maskAll) {
	    	 modelDisplay.doMaskAll();
	    	 modelDisplay.repaintLastChoice();
	    } else if(arg0.getSource() == this.maskRemove) {
	    	 modelDisplay.doMaskRemove();
	    	 modelDisplay.repaintLastChoice();
	    } else if(arg0.getSource() == this.maskInvert) {
	    	 modelDisplay.doMaskInvert();
	    	 modelDisplay.repaintLastChoice();
	    } else if(arg0.getSource() == this.maskSetDepth) {
	    	if (modelDisplay.isMasked()) {
		    	try {
			    	 float dep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "Depth to set:",
								"Setting a fixed depth for masked area", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
			    	 if ((dep > 0)) {
			    		 modelDisplay.doMaskSetDepth(dep);
				    	 modelDisplay.repaintLastChoice();
			    	 }
		    	} catch (Exception ex) {
		    		log(ex.toString(),LogLevel.debug);
		    	} // try / catch
	    	} // if (modelDisplay.isMasked())
	    } else if(arg0.getSource() == this.maskDivideByTwo) {
	    	if (modelDisplay.isMasked()) {
	    		modelDisplay.doDivideBy(2.0f);
	    		modelDisplay.repaintLastChoice();
	    	}
	    	
	    } else if(arg0.getSource() == this.maskDivideByThree) {
	    	if (modelDisplay.isMasked()) {
	    		modelDisplay.doDivideBy(3.0f);
	    		modelDisplay.repaintLastChoice();
	    	}
	    }
	}
	
	@SuppressWarnings("rawtypes")
	private String getValidBearerToken() throws Exception {
		
		SeeneAPI api = new SeeneAPI(pd);
		
		// Step 1 - try to read bearer token from config file
		seeneBearerToken = getParameterFromConfiguration(configFile, "api_token");
		
		// Step 2 - test the stored bearer token
		if ((seeneBearerToken!=null) && (seeneBearerToken.length() > 0)) {
			if (testBearerToken(seeneBearerToken)) {
				log("stored bearer token still valid!", LogLevel.debug);
				return seeneBearerToken;
			} else { // Step 3 - if test fails, try to refresh bearer token
				log("bearer token expired! trying to get new one...", LogLevel.debug);
				seeneAuthorizationCode = getParameterFromConfiguration(configFile, "auth_code");
				if (seeneAuthorizationCode.length() > 0) {
					// try to get new bearer token with refresh_token from the seene API
					Map response = api.requestBearerToken(seeneUser, seenePass, seeneAuthorizationCode, true);
					seeneBearerToken = (String)response.get("access_token");
					seeneAuthorizationCode = (String)response.get("refresh_token");
					insertOrReplaceConfigParameter(configFile, "api_token", seeneBearerToken);
					insertOrReplaceConfigParameter(configFile, "auth_code", seeneAuthorizationCode);
					
					return seeneBearerToken;
				}
			}
			
		} else { // No bearer token found!
			// try to read authorization code from config file
			seeneAuthorizationCode = getParameterFromConfiguration(configFile, "auth_code");
			if ((seeneAuthorizationCode!=null) && (seeneAuthorizationCode.length() > 0)) {
				// try to get bearer token from the seene API
				Map response = api.requestBearerToken(seeneUser, seenePass, seeneAuthorizationCode, false);
				seeneBearerToken = (String)response.get("access_token");
				seeneAuthorizationCode = (String)response.get("refresh_token");
				insertOrReplaceConfigParameter(configFile, "api_token", seeneBearerToken);
				insertOrReplaceConfigParameter(configFile, "auth_code", seeneAuthorizationCode);
				
				return seeneBearerToken;
				
			} else {
				// NO authorization code AND NO bearer token available!
				log("Seene-Toolkit seems not to be authorized to use the Seene API.",LogLevel.warn);
				log("Please visit: " + STK.AUTHORIZE_URL, LogLevel.info);
				showAuthorizationDialog();
			} 
		}
		
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private Boolean testBearerToken(String token) {
		SeeneAPI api = new SeeneAPI(pd);
		try {
			String userId = api.usernameToId(seeneUser);
			Map response = api.requestUserInfo(userId, token);
			String username = (String)response.get("username");
			if (username.equalsIgnoreCase(seeneUser)) return true;
		} catch (Exception e) {
			log("Test failed: " + e.getMessage(),LogLevel.warn);
		}
		
		return false;
	}
	
	
	private void insertOrReplaceConfigParameter(File cf, String param, String newValue) {
		String paramEq = new String(param + "=");
		if(cf.exists() && !cf.isDirectory()) {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(cf));
				String line;
				String newConf = "";
				Boolean replaced = false;
    			while ((line = br.readLine()) != null) {
    				if ((line.length()>paramEq.length()) && (line.substring(0, paramEq.length()).equalsIgnoreCase(paramEq))) {
    					newConf += paramEq + newValue + '\n';
    					log("replaced " + param + " in " + cf.getAbsolutePath(),LogLevel.info);
    					replaced = true;
    				} else {
    					newConf += line + '\n';
    				}
    			}
    			// Parameter not replaced? -> append to config!
    			if (!replaced) newConf += paramEq + newValue + '\n';
    			br.close();
    			
    			// Write new config file
    			FileOutputStream fileOut = new FileOutputStream(cf.getAbsolutePath());
    	        fileOut.write(newConf.getBytes());
    	        fileOut.flush();
    	        fileOut.close();
    	        
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
		} // if (f.exists()...
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
		    	   File fPNG = new File(files[i].getAbsolutePath() + File.separator + "folder.png");
		    	   if (!fPNG.exists()) {
		    		   Helper.createFolderIcon(files[i], null);
		    	   }
			       ImageIcon imgDir = new ImageIcon(fPNG.getAbsolutePath());
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
				// check which files exist and which menu items are enabled therefore
				File mf = new File(currentSelection.getToolTipText() + File.separator + STK.SEENE_MODEL);
				File tf = new File(currentSelection.getToolTipText() + File.separator + STK.SEENE_TEXTURE);
				File xf = new File(currentSelection.getToolTipText() + File.separator + STK.XMP_COMBINED_JPG);
				File of = new File(currentSelection.getToolTipText() + File.separator + STK.XMP_ORIGINAL_JPG);
				File pf = new File(currentSelection.getToolTipText() + File.separator + STK.XMP_DEPTH_PNG);
				if (mf.exists()) rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_MODEL_OEMODEL).setEnabled(true);
				else rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_MODEL_OEMODEL).setEnabled(false);
				if (tf.exists()) rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_TEXTURE_POSTER).setEnabled(true);
				else rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_TEXTURE_POSTER).setEnabled(false);
				if (of.exists()) rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_TEXTURE_POSTER_ORIGINAL).setEnabled(true);
				else rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_TEXTURE_POSTER_ORIGINAL).setEnabled(false);
				if (pf.exists()) rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_MODEL_PNG).setEnabled(true);
				else rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_MODEL_PNG).setEnabled(false);
				if (xf.exists()) {
					rClickPopup.getComponent(STK.POPUP_POSITION_GENERATE_XMP).setEnabled(false);
					rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_TEXTURE_XMP).setEnabled(true);
					rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_MODEL_XMP).setEnabled(true);
				}
				else {
					rClickPopup.getComponent(STK.POPUP_POSITION_GENERATE_XMP).setEnabled(true);
					rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_TEXTURE_XMP).setEnabled(false);
					rClickPopup.getComponent(STK.POPUP_POSITION_LOAD_MODEL_XMP).setEnabled(false);
				}
				// show the popup menu
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
		
		String seenePath = seeneFolder.getAbsolutePath();
		String offlinePath = storage.getOfflineDir().getAbsolutePath();
		
		// Set Localname if Seene is loaded from the "Offline" pool
		if (seenePath.toLowerCase().contains(offlinePath.toLowerCase())) {
			currentSeene.setLocalname(seenePath.substring(offlinePath.length()+1));
		}

		File mf = currentSeene.getModelFile();
		File tf = currentSeene.getPosterFile();
		File xf = currentSeene.getXMP_combined();
		File df = currentSeene.getXMP_depthpng();
		File of = currentSeene.getXMP_original();
		
		String loadmode = "unloadable: no loadable model/texture combination found";
		
		// Trying to find a loadable combo
		// Prio 5 - MIX: we have depthmap PNG and a proprietary Seene poster.
		if ((df.exists()) && (tf.exists())) loadmode="model:png,image:proprietary";
		// Prio 4 - MIX: we have proprietary Seene Model and a vertical oriented poster.
		if ((mf.exists()) && (of.exists())) loadmode="model:proprietary,image:original";
		// Prio 3 - we have a XMP (Model and Texture in one file).
		if (xf.exists()) loadmode="model:xmp,image:xmp";
		// Prio 2 - we have depthmap PNG and a vertical oriented poster.
		if ((df.exists()) && (of.exists())) loadmode="model:png,image:original";
		// Prio 1 - we have original proprietary Seene files.
		if ((mf.exists()) && (tf.exists())) loadmode="model:proprietary,image:proprietary";
		
		log("loadmode: " + loadmode,LogLevel.info);
		
		if (loadmode.indexOf("unloadable")>=0) {
			log(loadmode,LogLevel.error);
		} else {
			if (loadmode.indexOf("model:png")>=0) {
				log("Loading PNG-Image as Seene Model: " +  mf.getAbsolutePath(),LogLevel.info);
				currentSeene.getModel().loadModelDataFromPNG(df, xf);
			}
			if (loadmode.indexOf("image:original")>=0) {
				log("Loading original JPG Image: " + of.getAbsolutePath() ,LogLevel.info);
				currentSeene.setPoster(loadXMPOriginalPoster(seenePath));
			}
			if (loadmode.indexOf("model:proprietary")>=0) {
				log("Loading proprietary Seene Model: " +  mf.getAbsolutePath(),LogLevel.info);
				currentSeene.getModel().loadModelDataFromFile();
			}
			if (loadmode.indexOf("image:proprietary")>=0) {
				log("Loading proprietary Seene Texture: " +  tf.getAbsolutePath(),LogLevel.info);
				currentSeene.getPoster().loadTextureFromFile();
			}
			if (loadmode.indexOf("model:xmp")>=0) {
				log("Loading XMP Model: " + xf.getAbsolutePath() ,LogLevel.info);
				currentSeene.setModel(loadXMPCombinedModel(seenePath));
			}
			if (loadmode.indexOf("image:xmp")>=0) {
				log("Loading XMP Image: " + xf.getAbsolutePath() ,LogLevel.info);
				currentSeene.setPoster(loadXMPCombinedPoster(seenePath));
			}
			
			log("Model width: " +  currentSeene.getModel().getDepthWidth(),LogLevel.info);
			log("Model height: " +  currentSeene.getModel().getDepthHeight() ,LogLevel.info);
			normalizer.setNormMaxFloat(currentSeene.getModel().getMaxFloat());
			normalizer.setNormMinFloat(currentSeene.getModel().getMinFloat());
			modelDisplay.setModel(currentSeene.getModel());
			modelDisplay.setPoster(currentSeene.getPoster());
		}
		
	}
	
	
	@SuppressWarnings("serial")
	class ModelGraphics extends Canvas {
		
		public SeeneObject seeneObject;
		public SeeneModel model; 
		public SeeneTexture poster;
		private List<Boolean> mask = new ArrayList<Boolean>();
		int maskBrushRadius = 2;
		public String lastChoice = "";
		float rememberedFloat = STK.INIT_DEPTH;

		public int canvasSize=STK.WORK_WIDTH;
		public int pointSize=2;
		public boolean inverted=true;
		
		public ModelGraphics(){
			setSize(canvasSize*getPointSize(), canvasSize*getPointSize());	
	        setBackground(Color.white);
	        
	        // MouseWheel to change size of the mask brush
	        addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					int steps = e.getWheelRotation();
					maskBrushRadius += steps;
					if (maskBrushRadius<0) maskBrushRadius=0;
					if (maskBrushRadius>8) maskBrushRadius=8;
					log("new Mask Brush Radius: " + maskBrushRadius,LogLevel.debug);
					setMaskCursorWithSize(maskBrushRadius);
				}
			});
	        
	        // MouseListener for Painting the Mask
	        addMouseListener(new MouseAdapter(){
	        	int w = 0;
	        	int h = 0;
	        	int ndx = 0;
	        	int mOffX = 0;
	        	int mOffY = 0;
	        	int last_n = 0;
	        	Boolean maskPaintMode;
	        	
	        	volatile private boolean mouseDown = false;
	        	
	        	public void mouseEntered(MouseEvent e) {
    	        	setMaskCursorWithSize(maskBrushRadius);
	        	}
	        	
	        	public void mousePressed(MouseEvent e) {
	        	    if ((e.getButton() == MouseEvent.BUTTON1) || 
	        	    	(e.getButton() == MouseEvent.BUTTON2) ||
	        	    	(e.getButton() == MouseEvent.BUTTON3)) {
	        	    	if (e.getButton() == MouseEvent.BUTTON1) maskPaintMode = true;
	        	    	if (e.getButton() == MouseEvent.BUTTON3) maskPaintMode = false;
	        	        mouseDown = true;
	        	        if (model!=null) {
	        	        	w = model.getDepthWidth();
	        	        	h = model.getDepthHeight();
	        	        	ndx = w * h;
	        	        	mOffX = MouseInfo.getPointerInfo().getLocation().x - e.getX();
	        	        	mOffY = MouseInfo.getPointerInfo().getLocation().y - e.getY();
	        	        	// DepthPoint Info
	        	        	if (e.getButton() != MouseEvent.BUTTON2) {
	        	        		initMaskPaintThread();
	        	        	} else {
		        	        	float max = model.getMaxFloat();
		        	        	int mx = w - e.getX() / pointSize - 1;
	    						int my = e.getY() / pointSize;
	    						int n = mx * w + my;
	    						float f = model.getFloats().get(n);
	    						float cf = floatGreyScale(f, max);
		        	        	log("\nPOSITION: x: " + mx +  " - y: " + my + " (float number: " + n + ")\nCOLOR: " + cf + "\nDEPTH: " + f,LogLevel.info);
		        	        	setRememberedFloat(f);
	        	        	}
	        	        }
	        	    }
	        	}
	        	
	        	public void mouseReleased(MouseEvent e) {
	        		if ((e.getButton() == MouseEvent.BUTTON1) || (e.getButton() == MouseEvent.BUTTON3)) {
	        	        mouseDown = false;
	        	        if (lastChoice=="model") repaintModelOnly();
	        	        if (lastChoice=="poster") repaintPosterOnly();
	        	    }
	        	}

	        	volatile private boolean isRunning = false;
	        	
	        	private synchronized boolean checkAndMark() {
	        	    if (isRunning) return false;
	        	    isRunning = true;
	        	    return true;
	        	}
	        	
	        	private void initMaskPaintThread() {
	        	    if (checkAndMark()) {
	        	        new Thread() {
	        	            public void run() {
	        	                do {
	        	                	int mx = w - (MouseInfo.getPointerInfo().getLocation().x - mOffX) / pointSize - 1;
	        						int my = (MouseInfo.getPointerInfo().getLocation().y - mOffY) / pointSize;
	        						int n=0;
	        						if (maskBrushRadius>0) {
	        							for (int bx=0-maskBrushRadius;bx<maskBrushRadius;bx++) {
	        								for (int by=0-maskBrushRadius;by<maskBrushRadius;by++) {
	        									n = (mx + bx) * w + (my + by);
	        									if ((n >= 0) && ( n < ndx) && ((my + by) < h) && ((my + by) >= 0)) mask.set(n, maskPaintMode);
	        								}
	        							}
	        						} else {
	        							n = mx * w + my;
	        							if ((n >= 0) && ( n < ndx) && (my < h) && (my >= 0)) mask.set(n, maskPaintMode);
	        							System.out.println(my);
	        						}
	        						if (n!=last_n) {
	        							repaintLastChoice();
	        							last_n=n;
	        						} else { repaintMaskOnly(); }
	        						
	        	                } while (mouseDown);
	        	                isRunning = false;
	        	            }
	        	        }.start();
	        	    }
	        	}
			});
	    }

		public void paint(Graphics g){
	    	paintModel(g,model);
	    	paintPoster(g, poster);
	    	paintMask(g);
	    }
	    
	    private void paintMask(Graphics g) {
	    	if (model!=null) {
		    	Color mc = new Color(1f,0f,0f,.4f );
		    	g.setColor(mc);
	    		
		    	int c=0;
		    	Boolean masked = false;
	    		int w = model.getDepthWidth();
		        int h = model.getDepthHeight();
		        int p = getPointSize();
		        
		        for (int x=0;x<w;x++) {
		        	for (int y=0;y<h;y++) {
		        		masked = mask.get(c);
		        		if (masked) g.fillRect((w-x-1)*p, y*p , p, p);
		        		c++;
		        	} // for y
		        } //  for x
	    	}
	    }
	    
	    private void paintPoster(Graphics g, SeeneTexture poster) {
	    	if (poster!=null) {
	    		Image texture = poster.getTextureImage();
	    		int new_width = canvasSize*getPointSize();
	    		int new_height = canvasSize*getPointSize();
	    		BufferedImage textureTransformed = Helper.rotateAndResizeImage(texture, new_width, new_height,90);
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
		        		c++;
		        	} // for y
		        } //  for x
	    	} // if (model!=null)
	    }
	    
	    private float floatGreyScale(float value, float maximum) {
	    	if (inverted) return value/maximum;
	    	return 1 - value/maximum;
	    }
	    
	    private void repaintLastChoice() {
	    	if (lastChoice=="model") repaintModelOnly();
			if (lastChoice=="poster") repaintPosterOnly();
	    }
	    
	    private void repaintGraphics() {
	    	if ((model!=null) && (poster!=null)) {
				Graphics g = getGraphics();
				setSize(model.getDepthWidth()*getPointSize(), model.getDepthHeight()*getPointSize());
				this.paint(g);
			}
	    }
	    
	    public void repaintMaskOnly() {
	    	if (model!=null) {
				Graphics g = getGraphics();
				this.paintMask(g);
			}
	    }
	    
	    public void repaintModelOnly() {
	    	if (model!=null) {
	    		lastChoice="model";
				Graphics g = getGraphics();
				//setSize(model.getDepthWidth()*getPointSize(), model.getDepthHeight()*getPointSize());
				this.paintModel(g, model);
				this.paintMask(g);
			}
	    }
	    
	    public void repaintPosterOnly() {
	    	if (poster!=null) {
	    		lastChoice="poster";
				Graphics g = getGraphics();
				this.paintPoster(g, poster);
				this.paintMask(g);
			}
	    }
	    
	    private void setMaskCursorWithSize(int radius) {
			//TODO recreate cursors as 32x32 pics!
			Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			Image image = toolkit.getImage(SeeneToolkit.class.getResource("/images/cursor" + radius + ".png"));
			Cursor cu = toolkit.createCustomCursor(image , new Point((radius + radius / 2) + 1, (radius + radius / 2) + 1), "");
			setCursor(cu);
		}
	    
	    // Mask Operation Methods
	    public void doMaskAll() {
	    	if (model!=null) mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), true));
	    }
	    
	    public void doMaskRemove() {
	    	if (model!=null) mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    }
	    
	    public void doMaskInvert() {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			mask.set(n, !mask.get(n));
	    		}
	    	}
	    }
	    
	    public void doMaskSetDepth(float dep) {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) model.getFloats().set(n, dep);
	    		}
	    		model=findModelExtema(model);
	    	}
		}
	    
	    public void doDivideBy(float div) {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) model.getFloats().set(n, model.getFloats().get(n) / div);
	    		}
	    		model=findModelExtema(model);
	    	}
		}
	    
	    public Boolean isMasked() {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) return true;
	    		}
	    	}
	    	return false;
		}
	    
	    
	    // Getter and Setter
	    public SeeneObject getSeeneObject() {
			return seeneObject;
		}
		public void setSeeneObject(SeeneObject seeneObject) {
			this.seeneObject = seeneObject;
			setModel(seeneObject.getModel());
			setPoster(seeneObject.getPoster());
			repaintLastChoice();
		}
	    public SeeneModel getModel() {
			return model;
		}
		public void setModel(SeeneModel model) {
			this.model = model;
			doMaskRemove();
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
		public float getRememberedFloat() {
			return rememberedFloat;
		}
		public void setRememberedFloat(float rememberedFloat) {
			this.rememberedFloat = rememberedFloat;
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
	
	private void showUploadDialog() {
		JDialog uploadDialog = new JDialog();
		
		uploadDialog.setTitle("Upload to your private Seenes");
		uploadDialog.setSize(500, 300);
		uploadDialog.setLocationRelativeTo(mainFrame);
		uploadDialog.setModal(true);
		
		JPanel gridPanel = new JPanel();
    	gridPanel.setLayout(new java.awt.GridLayout(4,2));
    	
    	JLabel labelCaption = new JLabel(" Caption: ");
    	JTextArea tfCaption = new JTextArea(currentSeene.getCaption());
    	tfCaption.setLineWrap(true);
    	tfCaption.setBorder(BorderFactory.createEtchedBorder());
    	
    	JLabel labelCapDate = new JLabel(" Captured at: ");
    	JLabel tfCapDate = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentSeene.getCaptured_at()));
    	
    	JButton buttonOK = new JButton("upload");
    	buttonOK.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
            	currentSeene.setCaption(tfCaption.getText());
            	doUploadSeene(currentSeene);
            	uploadDialog.remove(gridPanel);
            	uploadDialog.dispose();
            }
    	});
		
		JButton buttonCancel = new JButton("cancel");
    	buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	uploadDialog.remove(gridPanel);
            	uploadDialog.dispose();
            }
        });
    	
    	gridPanel.add(labelCaption);
    	gridPanel.add(tfCaption);
    	gridPanel.add(labelCapDate);
    	gridPanel.add(tfCapDate);
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(buttonOK);
    	gridPanel.add(buttonCancel);
    	
    	uploadDialog.add(gridPanel);
    	//uploadDialog.pack();
    	
    	uploadDialog.setVisible(true);
		
		
	}
	
	private void showAuthorizationDialog() {
		
		authorizationDialog.setTitle("Authorize Seene-Toolkit for your account!");
		authorizationDialog.setSize(640,280);
		authorizationDialog.setLocationRelativeTo(mainFrame);
		authorizationDialog.setModal(true);

		JPanel gridPanel = new JPanel();
    	gridPanel.setLayout(new java.awt.GridLayout(10,1));
    	
    	JLabel labelHint1 = new JLabel("If you want to use the upload feature, you have to authorize seene-toolkit to do uploads to your Seene account!");
    	JLabel labelHint2 = new JLabel("Please start the authorization process by pressing the button below.");
    	JLabel labelHint3 = new JLabel("(Your webbrowser will open. Login to Seene and copy the authorization code that you will get in the input field below)");
    	JButton startAuthButton = new JButton("start autorization");
        JLabel labelAuthCode = new JLabel("Insert Seene Authorization Code below:");
        JTextField tfAuthCode = new JTextField();
        JButton buttonOK = new JButton("OK");
        JButton buttonCancel = new JButton("cancel");
        
        startAuthButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					Helper.openWebpage(new URL(STK.AUTHORIZE_URL));
				} catch (MalformedURLException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
            }
    	});
        
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (tfAuthCode.getText().length()>40) {
					insertOrReplaceConfigParameter(configFile, "auth_code", tfAuthCode.getText());
					authorizationDialog.remove(gridPanel);
	            	authorizationDialog.dispose();
				}
            }
    	});
    	
    	buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	authorizationDialog.remove(gridPanel);
            	authorizationDialog.dispose();
            }
        });
        
    	labelHint1.setHorizontalAlignment(JLabel.CENTER);
    	labelHint2.setHorizontalAlignment(JLabel.CENTER);
    	labelHint3.setHorizontalAlignment(JLabel.CENTER);
    	labelAuthCode.setHorizontalAlignment(JLabel.CENTER);
    	
    	JPanel gridAuthButton = new JPanel();
    	gridAuthButton.setLayout(new java.awt.GridLayout(1,3));
    	
    	gridAuthButton.add(new JLabel(""));
    	gridAuthButton.add(startAuthButton);
    	gridAuthButton.add(new JLabel(""));
    	
    	JPanel gridOKCancel = new JPanel();
    	gridOKCancel.setLayout(new java.awt.GridLayout(1,4));
    	
    	gridOKCancel.add(new JLabel(""));
    	gridOKCancel.add(buttonOK);
    	gridOKCancel.add(buttonCancel);
    	gridOKCancel.add(new JLabel(""));
    	
    	gridPanel.add(labelHint1);
    	gridPanel.add(labelHint2);
    	gridPanel.add(labelHint3);
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(gridAuthButton);
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(labelAuthCode);
    	gridPanel.add(tfAuthCode);
    	gridPanel.add(new JLabel(""));
    	gridPanel.add(gridOKCancel);

    	authorizationDialog.add(gridPanel);
    	authorizationDialog.setVisible(true);
	}
	
	private void showSettingsDialog() {
		
		readConfiguration(configFile);
		
    	settingsDialog.setTitle("Seene-Club Settings");
    	settingsDialog.setSize(400, 160);
    	settingsDialog.setLocationRelativeTo(mainFrame);
    	settingsDialog.setModal(true);
    	
    	JPanel gridPanel = new JPanel();
    	gridPanel.setLayout(new java.awt.GridLayout(5,2));
    	
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
							writer.println("api_id=" + STK.CONFIG_API_ID_HINT);
						}
						writer.println("storage=" + tfLocalStorage.getText());
						writer.println("username=" + tfUsername.getText());
						if (tfPassphrase.getText().length() > 0) {
							 if (!tfPassphrase.getText().equals("{unchanged}")) writer.println("passphrase=" + XOREncryption.xorIt(tfPassphrase.getText()));
							 if (tfPassphrase.getText().equals("{unchanged}")) writer.println("passphrase=" + XOREncryption.xorIt(seenePass));
						}
						
						// Authorization Code
						if ((seeneAuthorizationCode!=null) && (seeneAuthorizationCode.length() > 0)) writer.println("auth_code=" + seeneAuthorizationCode);
						else writer.println("auth_code="+ STK.CONFIG_AUTH_CODE_HINT);
						
						// Bearer Token
						if ((seeneBearerToken!=null) && (seeneBearerToken.length() > 0)) writer.println("api_token=" + seeneBearerToken);
						
						// Write Proxy Settings (not in dialog)
						writer.println("proxy.host=" + pd.getHost());
						writer.println("proxy.port=" + pd.getPortString());
						writer.println("proxy.user=" + pd.getUser());
						writer.println("proxy.pass=" + pd.getPass());
						
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
    				if ((line.length()>paramEq.length()) && (line.substring(0, paramEq.length()).equalsIgnoreCase(paramEq))) {
    					log("configured " + paramEq + " is: " + line.substring(paramEq.length()),LogLevel.debug);
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
    				if ((line.length() >= 7) && (line.substring(0, 7).equalsIgnoreCase("api_id="))) {
    					log("configured api_id is: " + line.substring(7),LogLevel.debug);
    					seeneAPIid = line.substring(7);
    				}
    				if ((line.length() >= 8) && (line.substring(0, 8).equalsIgnoreCase("storage="))) {
    					log("configured storage path: " + line.substring(8),LogLevel.debug);
    					storage.setPath(line.substring(8));
    					storageOK = storage.initializer();
    				}
    				if ((line.length() >= 9 ) && (line.substring(0, 9).equalsIgnoreCase("username="))) {
    					log("configured username: " + line.substring(9),LogLevel.debug);
    					seeneUser = line.substring(9);
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("passphrase="))) {
    					log("configured passphrase: " + line.substring(11),LogLevel.debug);
    					seenePass = XOREncryption.xorIt(line.substring(11));
    				}
    				if ((line.length() >= 10) && (line.substring(0, 10).equalsIgnoreCase("auth_code="))) {
    					log("configured authorization code: " + line.substring(10),LogLevel.debug);
    					seeneAuthorizationCode = line.substring(10);
    				}
    				if ((line.length() >= 10) && (line.substring(0, 10).equalsIgnoreCase("api_token="))) {
    					log("configured Bearer token: " + line.substring(10),LogLevel.debug);
    					seeneBearerToken = line.substring(10);
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.host="))) {
    					log("configured proxy.host: " + line.substring(11),LogLevel.debug);
    					pd.setHost(line.substring(11));
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.port="))) {
    					log("configured proxy.port: " + line.substring(11),LogLevel.debug);
    					pd.setPort(line.substring(11));
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.user="))) {
    					log("configured proxy.user: " + line.substring(11),LogLevel.debug);
    					pd.setUser(line.substring(11));
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.pass="))) {
    					log("configured proxy.pass: " + line.substring(11),LogLevel.debug);
    					pd.setPass(line.substring(11));
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

