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
import javax.swing.JComboBox;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.seeneclub.domainvalues.LogLevel;
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
	
	static String programVersion = "0.93"; 
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
    JButton tbShow3D = new JButton("show 3D plot");
    JButton tbSaveMask = new JButton("save mask");
    JLabel tbLoadMaskLabel = new JLabel(" load mask: ");
    JComboBox<String> tbLoadMaskCombo = new JComboBox<String>();
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
    static JDialog authorizationDialog = new JDialog();
	
	// Task Menu Items
    JMenuItem taskBackupPublic = new JMenuItem("retrieve my public seenes");
    JMenuItem taskBackupPrivate = new JMenuItem("retrieve my private seenes");
    JMenuItem taskBackupOther = new JMenuItem("retrieve someone else's seenes");
    JMenuItem taskBackupByURL = new JMenuItem("retrieve public seene by URL");
    JMenuItem taskBackupAllMySets = new JMenuItem("backup all of my sets");
    JMenuItem taskBackupAllSets = new JMenuItem("backup someone else's sets");
    JMenuItem taskBackupAnySet = new JMenuItem("backup any set by URL");
    
    
    // Mask Menu Items
    JMenuItem maskUndo = new JMenuItem("undo");
    JMenuItem maskRedo = new JMenuItem("redo");
    JMenuItem maskAll = new JMenuItem("mask all");
    JMenuItem maskRemove = new JMenuItem("remove mask");
    JMenuItem maskInvert = new JMenuItem("invert mask");
    JMenuItem maskSelectByDepth = new JMenuItem("select mask by depth value");
    JMenuItem maskSelectByRange = new JMenuItem("select mask by depth range");
    JMenuItem maskSetDepthSmooth = new JMenuItem("set depth (smooth edges)");
    JMenuItem maskSetDepth = new JMenuItem("set depth (hard edges)");
    JMenuItem maskSetHemisphere = new JMenuItem("hemisphere convex");
    JMenuItem maskSetHemisphereConcave = new JMenuItem("hemisphere concave");
    JMenuItem maskGradientUpwards = new JMenuItem("depthmap gradient upwards");
    JMenuItem maskGradientUpwardsToRight = new JMenuItem("depthmap gradient upwards to right");
    JMenuItem maskGradientLeftToRight = new JMenuItem("depthmap gradient left to right");
    JMenuItem maskGradientDownwardsToRight = new JMenuItem("depthmap gradient downwards to right");
    JMenuItem maskGradientDownwards = new JMenuItem("depthmap gradient downwards");
    JMenuItem maskGradientDownwardsToLeft = new JMenuItem("depthmap gradient downwards to left");
    JMenuItem maskGradientRightToLeft = new JMenuItem("depthmap gradient right to left");
    JMenuItem maskGradientUpwardsToLeft = new JMenuItem("depthmap gradient upwards to left");
    JMenuItem maskRiseLower = new JMenuItem("rise or lower depth in masked area");
    JMenuItem maskDivideByTwo = new JMenuItem("devide depth in masked area by 2");
    JMenuItem maskDivideByThree = new JMenuItem("devide depth in masked area by 3");
    
       
    // Tests Menu Items
    JMenuItem testSomething = new JMenuItem("Test Something");
    
    // Seene Object we are currently working on
    SeeneObject currentSeene = null;
    Boolean currentHasChanges = false;
    JLabel currentSelection = null;
    String currentMaskName = new String("");
    SeeneNormalizer normalizer = new SeeneNormalizer();

   
    // method main - all begins with a thread!
	public static void main(String[] args) throws Exception {
		
		configDir = new File(System.getProperty("user.home") + File.separator + ".seene-club");
		if (configDir.exists() || configDir.mkdirs()) {
			configFile = new File(configDir + File.separator + "configuration");
		}
		
		new Thread(new SeeneToolkit()).start();
	}

    
    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    
    private static void doTaskBackupSeenes(File targetDir, String username, int count, Boolean privat_flag) {
    	try {
    		String privat = new String();
    		if (privat_flag) {
    			privat = "true";
    			log("Private Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    		} else {
    			privat = "false";
    			if (username.equals(seeneUser)) {
    				log("Public Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    			} else {
    				log("Others Seenes will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    			}
    		}
    		
        	SeeneAPI myAPI = new SeeneAPI(pd);
        	log("Resolving " + username + " to ID", LogLevel.info);
			String userID = myAPI.requestUserID(username, getValidBearerToken());
			log("Seene user: " + userID, LogLevel.debug);
			
			int pages = ((count-1) / 100) + 1;
    		int rest = count;
    		
    		log(count + " Seenes = " + pages + " pages",LogLevel.info);

    		List<SeeneObject> index = new ArrayList<SeeneObject>();
    		
    		for (int page=1;page<=pages;page++) {
    			if (rest>=100) {
    			    log("Requesting 100 Seenes from Page: " + page,LogLevel.info);
    			    index = myAPI.requestUserSeenes(userID, page, 100, privat, getValidBearerToken());
    			} else {
    				log("Requesting " + rest + " Seenes from Page: " + page,LogLevel.info);
    				index = myAPI.requestUserSeenes(userID, page, rest, privat, getValidBearerToken());
    			}
    			
    			downloadInThreads(index, targetDir, STK.NUMBER_OF_DOWNLOAD_THREADS);	
    			rest-=100;
    		}
			
		} catch (Exception e) {
			StringBuffer inform = new StringBuffer("The following error occured:\n");
			inform.append(e.getMessage());
			inform.append("\n\nPlease check your Seene credentials configuration and your internet connection!");
            log(e.getMessage(),LogLevel.error);
            JOptionPane.showMessageDialog(null,  inform.toString(), "Backup Error", JOptionPane.ERROR_MESSAGE);
		}
    }

    
    private static void doTaskBackupByURL(File targetDir, String surl) {
    	try {
    		log("Seene will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    		
    		SeeneAPI myAPI = new SeeneAPI(pd);
    		
    		List<SeeneObject> index = myAPI.getPublicSeeneByURLoldAPI(surl);
    		downloadInThreads(index, targetDir, 1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	protected void doTaskBackupAllSets(File targetDir, String uid) {
		try {
			SeeneAPI myAPI = new SeeneAPI(pd);
		
			List<SeeneSet> sets = myAPI.getUsersSetList(uid);
			
			for(SeeneSet st : sets) {
				File seeneOriginalsDir = new File(targetDir.getAbsolutePath() + File.separator + st.getTitle() + " - 3D set by " + st.getUserid() + File.separator + ".seeneOriginals");
				List<SeeneObject> index = myAPI.getPublicSetByURLoldAPI("https://seene.co/a/" + st.getShortCode());
	    		downloadInThreads(index, seeneOriginalsDir, STK.NUMBER_OF_DOWNLOAD_THREADS);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    protected void doTaskBackupAnySet(File targetDir, String surl) {
    	try {
    		log("Set will go to " + targetDir.getAbsolutePath() ,LogLevel.info);
    		
    		SeeneAPI myAPI = new SeeneAPI(pd);
    		SeeneSet st = myAPI.getPublicSetInfoByURLoldAPI(surl);
    		File seeneOriginalsDir = new File(targetDir.getAbsolutePath() + File.separator + SeeneStorage.winCompatibleFileName(st.getTitle()) + " - 3D set by " + st.getUserid() + File.separator + ".seeneOriginals");
    		
    		List<SeeneObject> index = myAPI.getPublicSetByURLoldAPI(surl);
    		downloadInThreads(index, seeneOriginalsDir, STK.NUMBER_OF_DOWNLOAD_THREADS);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

				progressbar.setValue(0);
				progressbar.setMaximum(toDownloadIndex.size());
				
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
							progressbar.setValue(progressbar.getValue()+1);
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
        JMenu fileMenu = new JMenu("File ");
        
        JMenuItem itemOpenImage = new JMenuItem("Open image");
        JMenuItem itemOpenDepthmap = new JMenuItem("Open depthmap");
        JMenuItem itemExit = new JMenuItem("Exit");
        
        itemOpenImage.setIcon(Helper.iconFromImageResource("texture.png", 16));
        itemOpenDepthmap.setIcon(Helper.iconFromImageResource("model.png", 16));
        itemExit.setIcon(Helper.iconFromImageResource("exit.png", 16));
        
        itemOpenImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showOpenAnyImageDialog();
            }
        });
        itemOpenDepthmap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	showOpenAnyDepthmapDialog();
            }
        });
        itemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });
        
        
        fileMenu.add(itemOpenImage);
        fileMenu.add(itemOpenDepthmap);
        fileMenu.addSeparator();
        fileMenu.add(itemExit);
        
        JMenu taskMenu = new JMenu(" Tasks ");
        
        taskBackupPublic.setIcon(Helper.iconFromImageResource("download.png", 16));
        taskBackupPrivate.setIcon(Helper.iconFromImageResource("downloadp.png", 16));
        taskBackupOther.setIcon(Helper.iconFromImageResource("downloado.png", 16));
        taskBackupByURL.setIcon(Helper.iconFromImageResource("downloadu.png", 16));
        taskBackupAllMySets.setIcon(Helper.iconFromImageResource("download.png", 16));
        taskBackupAllSets.setIcon(Helper.iconFromImageResource("downloado.png", 16));
        taskBackupAnySet.setIcon(Helper.iconFromImageResource("downloadu.png", 16));
        
        taskBackupPublic.addActionListener(this);
        taskBackupPrivate.addActionListener(this);
        taskBackupOther.addActionListener(this);
        taskBackupByURL.addActionListener(this);
        taskBackupAllMySets.addActionListener(this);
        taskBackupAllSets.addActionListener(this);
        taskBackupAnySet.addActionListener(this);
        
        
        taskMenu.add(taskBackupPublic);
        taskMenu.add(taskBackupPrivate);
        taskMenu.add(taskBackupOther);
        taskMenu.add(taskBackupByURL);
        taskMenu.addSeparator();
        taskMenu.add(taskBackupAllMySets);
        taskMenu.add(taskBackupAllSets);
        taskMenu.add(taskBackupAnySet);
        
        
        JMenu maskMenu = new JMenu(" Mask ");
        
        maskUndo.setIcon(Helper.iconFromImageResource("maskUndo.png", 16));
        maskRedo.setIcon(Helper.iconFromImageResource("maskRedo.png", 16));
        maskAll.setIcon(Helper.iconFromImageResource("maskall.png", 16));
        maskRemove.setIcon(Helper.iconFromImageResource("masknothing.png", 16));
        maskInvert.setIcon(Helper.iconFromImageResource("maskinvert.png", 16));
        maskSelectByDepth.setIcon(Helper.iconFromImageResource("maskByValue.png", 16));
        maskSelectByRange.setIcon(Helper.iconFromImageResource("maskByRange.png", 16));
        maskSetDepth.setIcon(Helper.iconFromImageResource("masksetvalue.png", 16));
        maskSetDepthSmooth.setIcon(Helper.iconFromImageResource("maskSmooth.png", 16));
        maskSetHemisphere.setIcon(Helper.iconFromImageResource("maskSphereConvex.png", 16));
        maskSetHemisphereConcave.setIcon(Helper.iconFromImageResource("maskSphereConcave.png", 16));
        maskGradientUpwards.setIcon(Helper.iconFromImageResource("maskGradient0.png", 16));
        maskGradientUpwardsToRight.setIcon(Helper.iconFromImageResource("maskGradient45.png", 16));
        maskGradientLeftToRight.setIcon(Helper.iconFromImageResource("maskGradient90.png", 16));
        maskGradientDownwardsToRight.setIcon(Helper.iconFromImageResource("maskGradient135.png", 16));
        maskGradientDownwards.setIcon(Helper.iconFromImageResource("maskGradient180.png", 16));
        maskGradientDownwardsToLeft.setIcon(Helper.iconFromImageResource("maskGradient225.png", 16));
        maskGradientRightToLeft.setIcon(Helper.iconFromImageResource("maskGradient270.png", 16));
        maskGradientUpwardsToLeft.setIcon(Helper.iconFromImageResource("maskGradient315.png", 16));
        maskRiseLower.setIcon(Helper.iconFromImageResource("maskLift.png", 16));
        maskDivideByTwo.setIcon(Helper.iconFromImageResource("maskdividedby2.png", 16));
        maskDivideByThree.setIcon(Helper.iconFromImageResource("maskdividedby3.png", 16));
        
        maskUndo.addActionListener(this);
        maskRedo.addActionListener(this);
        maskAll.addActionListener(this);
        maskRemove.addActionListener(this);
        maskInvert.addActionListener(this);
        maskSelectByDepth.addActionListener(this);
        maskSelectByRange.addActionListener(this);
        maskSetDepthSmooth.addActionListener(this);
        maskSetDepth.addActionListener(this);
        maskSetHemisphere.addActionListener(this);
        maskSetHemisphereConcave.addActionListener(this);
        maskGradientUpwards.addActionListener(this);
        maskGradientUpwardsToRight.addActionListener(this);
        maskGradientLeftToRight.addActionListener(this);
        maskGradientDownwardsToRight.addActionListener(this);
        maskGradientDownwards.addActionListener(this);
        maskGradientDownwardsToLeft.addActionListener(this);
        maskGradientRightToLeft.addActionListener(this);
        maskGradientUpwardsToLeft.addActionListener(this);
        maskRiseLower.addActionListener(this);
        maskDivideByTwo.addActionListener(this);
        maskDivideByThree.addActionListener(this);
        
        maskMenu.add(maskUndo);
        maskMenu.add(maskRedo);
        maskMenu.addSeparator();
        maskMenu.add(maskAll);
        maskMenu.add(maskRemove);
        maskMenu.add(maskInvert);
        maskMenu.add(maskSelectByDepth);
        maskMenu.add(maskSelectByRange);
        maskMenu.addSeparator();
        maskMenu.add(maskSetDepthSmooth);
        maskMenu.add(maskSetDepth);
        maskMenu.addSeparator();
        maskMenu.add(maskSetHemisphere);
        maskMenu.add(maskSetHemisphereConcave);
        maskMenu.addSeparator();
        maskMenu.add(maskGradientUpwards);
        maskMenu.add(maskGradientUpwardsToRight);
        maskMenu.add(maskGradientLeftToRight);
        maskMenu.add(maskGradientDownwardsToRight);
        maskMenu.add(maskGradientDownwards);
        maskMenu.add(maskGradientDownwardsToLeft);
        maskMenu.add(maskGradientRightToLeft);
        maskMenu.add(maskGradientUpwardsToLeft);
        maskMenu.addSeparator();
        maskMenu.add(maskRiseLower);
        maskMenu.add(maskDivideByTwo);
        maskMenu.add(maskDivideByThree);
        
        maskUndo.setEnabled(false);
        maskRedo.setEnabled(false);
        
        JMenu clubMenu = new JMenu(" Seene Club");
        
        JMenuItem itemSettings = new JMenuItem("Settings");
        itemSettings.setIcon(Helper.iconFromImageResource("settings.png", 16));
        itemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showSettingsDialog();
            }
        });
        
        clubMenu.add(itemSettings);
        
        JMenu testMenu = new JMenu("Tests");
        testSomething.addActionListener(this);
        
        testMenu.add(testSomething);
                
        bar.add(fileMenu);
        bar.add(taskMenu);
        bar.add(maskMenu);
        bar.add(clubMenu);
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
             	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay upper left");
              }
              if(event.getSource() == twXtwUpperRight) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,1);
             	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay upper right");
              }
              if(event.getSource() == twXtwBottomLeft) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,4);
             	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay bottom left");
              }
              if(event.getSource() == twXtwBottomRight) {
             	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),2,2);
             	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay bottom right");
              }
              if(event.getSource() == tXtUpperLeft) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,7);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay upper left");
              }
              if(event.getSource() == tXtUpperCenter) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,4);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay upper center");
              }
              if(event.getSource() == tXtUpperRight) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,1);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay upper right");
              }
              if(event.getSource() == tXtMiddleLeft) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,8);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay middle left");
              }
              if(event.getSource() == tXtMiddleCenter) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,5);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay middle center");
              }
              if(event.getSource() == tXtMiddleRight) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,2);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay middle right");
              }
              if(event.getSource() == tXtBottomLeft) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,9);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay bottom left");
              }
              if(event.getSource() == tXtBottomCenter) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,6);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay bottom center");
              }
              if(event.getSource() == tXtBottomRight) {
            	  currentSeene = inlaySeene(currentSeene,currentSelection.getToolTipText(),3,3);
            	  modelDisplay.setSeeneObjectWithUndo(currentSeene,"inlay bottom right");
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
            	  if (confirmedOverwrite("model")) {
            		  currentSeene.setModel(loadProprietaryModel(currentSelection.getToolTipText()));
            		  modelDisplay.setModel(currentSeene.getModel());
            		  modelDisplay.repaintModelOnly();
            	  }
              }
              if(event.getSource() == miLoadModelFromPNG) {
            	  if (confirmedOverwrite("model")) {
            		  currentSeene.setModel(loadXMPDepthPNGModel(currentSelection.getToolTipText()));
            		  modelDisplay.setModel(currentSeene.getModel());
            		  modelDisplay.repaintModelOnly();
            	  }
              }
              if(event.getSource() == miLoadModelFromXMPCombined) {
            	  if (confirmedOverwrite("model")) {
            		  currentSeene.setModel(loadXMPCombinedModel(currentSelection.getToolTipText()));
            		  modelDisplay.setModel(currentSeene.getModel());
            		  modelDisplay.repaintModelOnly();
            	  }
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
	            		  currentSeene.setLocalpath(savePath.getAbsolutePath());
	            		  
	            		  // Save XMP components
	            		  generateXMP(savePath.getAbsoluteFile().toString());
	            		     
	            		  Helper.createFolderIcon(savePath, null);
	            		  parsePool(storage.getOfflineDir());
	            		  btPoolLocalSeenes.getModel().setSelected(true);
	            		  currentHasChanges = false;
	            		
	            	  } else {
	            		  JOptionPane.showMessageDialog(mainFrame,  "Aborted. Seene not saved!", "Seene not saved.", JOptionPane.ERROR_MESSAGE);
	            	  }
            	  } else { showNoSeeneThereDialog("Can't save!"); }
            		  
              }
              if(event.getSource() == tbSaveMask) {
            	  if (modelDisplay.isMasked()) {
            		  if ((currentSeene.getLocalname()!=null) && (currentSeene.getLocalname().length() > 0)) {
            			  String maskname = (String)JOptionPane.showInputDialog(mainFrame, "Give the mask a name:",
    	                          "Saving the mask", JOptionPane.PLAIN_MESSAGE, null, null, currentMaskName);
            			  
            			  if ((maskname != null) && (maskname.length() > 0)) {
    	            		  File savePath = new File(storage.getOfflineDir().getAbsolutePath() + File.separator + currentSeene.getLocalname() + 
    	            				  				   File.separator + "Masks" + File.separator + maskname + ".mask");

    	            		  savePath.getParentFile().mkdirs();
    	            		  modelDisplay.saveMask(savePath);
    	            		  currentMaskName = maskname;
    	            		  reloadMaskLoadCombo(new File(currentSeene.getLocalpath()));
    	            	  } else {
    	            		  JOptionPane.showMessageDialog(mainFrame,  "Aborted. Mask not saved!", "Mask not saved.", JOptionPane.ERROR_MESSAGE);
    	            	  }
            			  
            		  } else {
            			  JOptionPane.showMessageDialog(mainFrame,  "Seene was not saved locally!\n" + 
              					"You can only save masks for locally saved Seenes.\n" +
            					"Please use [ save local ] before saving the mask!\n\n", 
              					"Not a local Seene!", JOptionPane.WARNING_MESSAGE);
            		  }
            	  } else {
            		  JOptionPane.showMessageDialog(mainFrame,  "There's no masked area in the editor!\n" + 
            					"Please, draw the mask with your mouse while pressing the left mouse button.\n\n" , 
            					"No mask!", JOptionPane.WARNING_MESSAGE);
            	  }
              }
              if(event.getSource() == tbLoadMaskCombo) {
            	  if ((tbLoadMaskCombo.getSelectedItem() != null) && (tbLoadMaskCombo.getSelectedItem().toString().length() > 0)) {
            		  File maskFile = new File(currentSeene.getLocalpath() 
            				  					+ File.separator + "Masks" 
            				  					+ File.separator +  tbLoadMaskCombo.getSelectedItem() + ".mask");
            		  if (maskFile.exists()) {
            			  modelDisplay.loadMask(maskFile);
            			  currentMaskName = maskFile.getName().substring(0, maskFile.getName().lastIndexOf('.'));
            		  }
            	  }
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
              if(event.getSource() == tbShow3D) {
            	  if (modelDisplay.getModel()!=null) {
            		  modelDisplay.repaint3DOnly();
            	  } else { showNoSeeneThereDialog("Can't show 3D!"); }
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
        tbShow3D.addActionListener(toolbarListener);
        tbSaveMask.addActionListener(toolbarListener);
        tbLoadMaskCombo.addActionListener(toolbarListener);
        tbUploadSeene.addActionListener(toolbarListener);
        
        tbLoadMaskCombo.setLightWeightPopupEnabled(false);
        
        toolbar.add(tbSaveLocal);
        toolbar.addSeparator();
        toolbar.add(tbShowModel);
        toolbar.add(tbShowPoster);
        toolbar.add(tbShow3D);
        toolbar.addSeparator();
        toolbar.add(tbSaveMask);
        toolbar.add(tbLoadMaskLabel);
        toolbar.add(tbLoadMaskCombo);
        toolbar.addSeparator();
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
	
	protected void showOpenAnyDepthmapDialog() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Depthmaps","png", "oemodel");
		
		chooser.setFileFilter(filter);
		
		int fileChooserReturnValue = chooser.showDialog(null,"open image");

		if(fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
        	File dFile = chooser.getSelectedFile();
        	SeeneModel mO = new SeeneModel();
        	String fn = dFile.getName();
        	if (Helper.getFileExtension(fn).equalsIgnoreCase("png")) {
        		log("opening depthmap from PNG: " + dFile.getPath(),LogLevel.info);
        		mO.loadModelDataFromPNG(dFile, null);
        	}
        	if (Helper.getFileExtension(fn).equalsIgnoreCase("oemodel")) {
        		log("opening proprietary scene.oemodel: " + dFile.getPath(),LogLevel.info);
        		mO.loadModelDataFromFile(dFile);
        	}
        	
        	currentSeene.setModel(mO);
        	modelDisplay.setModel(mO);
        	modelDisplay.repaintModelOnly();
        	normalizer.setNormMaxFloat(currentSeene.getModel().getMaxFloat());
			normalizer.setNormMinFloat(currentSeene.getModel().getMinFloat());
		}
	}


	protected void showOpenAnyImageDialog() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG File","jpg");
		chooser.setFileFilter(filter);
		
		int fileChooserReturnValue = chooser.showDialog(null,"open image");
        
        // Check if chosen or canceled
        if(fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
        	File iFile = chooser.getSelectedFile();
        	log("opening image: " + iFile.getPath(),LogLevel.info);
    		BufferedImage textureImage = null;
    		SeeneTexture tO = new SeeneTexture();
    		SeeneModel mO = new SeeneModel();
    		if ((currentSeene == null) ||(modelDisplay.getModel() == null)) {
    			currentSeene = new SeeneObject();
    			currentSeene.setModel(mO);
    			modelDisplay.setSeeneObject(currentSeene);
    		}
    		
			try {
				textureImage = ImageIO.read(iFile);
				int h = textureImage.getHeight();
				int w = textureImage.getWidth();
				if (w > h) textureImage = textureImage.getSubimage((w/2) - (h/2) ,0 , h, h);
				if (h > w) textureImage = textureImage.getSubimage(0, (h/2) - (w/2), w, w);
				tO = new SeeneTexture(Helper.rotateAndResizeImage(textureImage, textureImage.getWidth(), textureImage.getHeight(), 270));
				currentSeene.setPoster(tO);
				modelDisplay.setPoster(currentSeene.getPoster());
				modelDisplay.repaintPosterOnly();
				
				JPEG image = new JPEG(iFile.getAbsolutePath());
				
				if (image.hasDepthMap()) {
					
					Object[] options = {"yes, load this depthmap", "no, leave my model unchanged"};
					int n = JOptionPane.showOptionDialog(mainFrame,
						    "The image you have selected contains a depthmap!\nDo you want to load depthmap too?\n\n",
						    "Depthmap found!",
						    JOptionPane.YES_NO_CANCEL_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[0]);
					if (n==0) {
						mO.loadModelDataFromXMPenhancedJPG(iFile);
						currentSeene.setModel(mO);
						modelDisplay.setModel(mO);
						modelDisplay.repaintModelOnly();
						normalizer.setNormMaxFloat(currentSeene.getModel().getMaxFloat());
						normalizer.setNormMinFloat(currentSeene.getModel().getMinFloat());
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
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
		if (image.hasDepthMap()) mO.loadModelDataFromXMPenhancedJPG(xFile);
		else log("There is no Depthmap in file " + xmpFilepath, LogLevel.warn);
		
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
		byte[] depthmap = image.getDepthMapFromFile(pFile.getAbsolutePath());
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
		log("Inlay Min: " +  inlayModel.getMinFloat(),LogLevel.info);
		log("Inlay Max: " +  inlayModel.getMaxFloat(),LogLevel.info);
		
		
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
		log("new extrema: min: " + min + " - max: " + max,LogLevel.info);
		mO.setMinFloat(min);
		mO.setMaxFloat(max);
		normalizer.setNormMaxFloat(mO.getMaxFloat());
		normalizer.setNormMinFloat(mO.getMinFloat());
		return mO;
	}


	// create all JSplitPanes for the GUI
	@SuppressWarnings("serial")
	private JSplitPane createSplitPanels() {
		
		// Split the Pool Selection and the Seene Folders View
		JSplitPane splitWestNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
			 private final int location = 35;
			    { setDividerLocation( location ); }
			    @Override
			    public int getDividerLocation() { return location ; }
			    @Override
			    public int getLastDividerLocation() { return location ; }
		};
		splitWestNorthSouth.setTopComponent(panelWestNorth);
		splitWestNorthSouth.setBottomComponent(scrollWestSouth);
		splitWestNorthSouth.setDividerSize(2);
		splitWestNorthSouth.setDividerLocation(35);
		splitWestNorthSouth.setResizeWeight(0.0);
		
		Dimension minimumSize = new Dimension(250, 500);
		splitWestNorthSouth.setMinimumSize(minimumSize);
		
		JSplitPane splitEastSouthPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitEastSouthPanel.setTopComponent(panelLogOutput);
		splitEastSouthPanel.setBottomComponent(panelProgressbar);
		splitEastSouthPanel.setDividerSize(0);
		splitEastSouthPanel.setDividerLocation(160);
		splitEastSouthPanel.setResizeWeight(1.0);
		
		JSplitPane splitMainViewPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
			 private final int location = 35;
			    { setDividerLocation( location ); }
			    @Override
			    public int getDividerLocation() { return location ; }
			    @Override
			    public int getLastDividerLocation() { return location ; }
		};
		splitMainViewPanel.setTopComponent(mainToolbarPanel);
		splitMainViewPanel.setBottomComponent(mainViewPanel);
		splitMainViewPanel.setDividerSize(2);
		splitMainViewPanel.setDividerLocation(35);
		splitMainViewPanel.setResizeWeight(0.0);
		
		
		JSplitPane splitEastNorthSouth = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
			 private final int location = 768 - 240;
			    { setDividerLocation( location ); }
			    @Override
			    public int getDividerLocation() { return location ; }
			    @Override
			    public int getLastDividerLocation() { return location ; }
		};
		splitEastNorthSouth.setTopComponent(splitMainViewPanel);
		splitEastNorthSouth.setBottomComponent(splitEastSouthPanel);
		splitEastNorthSouth.setDividerSize(2);
		splitEastNorthSouth.setDividerLocation(768-240);
		splitEastNorthSouth.setResizeWeight(1.0);
		
		// Split Navigation Area and Display Area
		JSplitPane splitWestEast = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {
			 private final int location = 250;
			    { setDividerLocation( location ); }
			    @Override
			    public int getDividerLocation() { return location ; }
			    @Override
			    public int getLastDividerLocation() { return location ; }
		};
		splitWestEast.setLeftComponent(splitWestNorthSouth);
		splitWestEast.setRightComponent(splitEastNorthSouth);
		splitWestEast.setOneTouchExpandable(true);
		splitWestEast.setDividerSize(2);
		splitWestEast.setDividerLocation(250);
		splitWestEast.setResizeWeight(0.0);
		
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
							doTaskBackupSeenes(storage.getPublicDir(),seeneUser, cnt, false);	
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
							doTaskBackupSeenes(storage.getPrivateDir(), seeneUser, cnt, true);
							parsePool(storage.getPrivateDir());
		            		btPoolPrivateSeenes.getModel().setSelected(true);
						}
					};
					dlThread.start();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,  "Invalid number entered. Aborting!", "Backup Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if(arg0.getSource() == this.taskBackupAnySet) {
			String surl = (String)JOptionPane.showInputDialog(mainFrame, "Enter the URL of the set to download:\n(example: https://seene.co/a/6UUQTG)",
                    "Retrieving a set by URL", JOptionPane.PLAIN_MESSAGE, null, null, "http://seene.co/a/");
			if ((surl != null) && (surl.length() > 0)) {
				Thread dlThread = new Thread() {
					public void run() {
						doTaskBackupAnySet(storage.getSetsDir(),surl);	
					}
				};
				dlThread.start();
			}
		} else if(arg0.getSource() == this.taskBackupAllSets) {
			String uid = (String)JOptionPane.showInputDialog(mainFrame, "Whose sets do you want to backup?",
                    "Retrieving someone else's sets", JOptionPane.PLAIN_MESSAGE, null, null, null);
			if ((uid != null) && (uid.length() > 0)) {
					Thread dlThread = new Thread() {
						public void run() {
							doTaskBackupAllSets(storage.getSetsDir(),uid);
						}
					};
					dlThread.start();
			}
		} else if(arg0.getSource() == this.taskBackupAllMySets) {
			String uid = new String(seeneUser);
			if ((uid != null) && (uid.length() > 0)) {
					Thread dlThread = new Thread() {
						public void run() {
							doTaskBackupAllSets(storage.getSetsDir(),uid);
						}
					};
					dlThread.start();
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
								doTaskBackupSeenes(storage.getOthersDir(),uid,cnt,false);
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
	     else if(arg0.getSource() == this.maskUndo) {
	    	 modelDisplay.doUndo();
	    } else if(arg0.getSource() == this.maskRedo) {
	    	modelDisplay.doRedo();
	    } else if(arg0.getSource() == this.maskAll) {
	    	 modelDisplay.doMaskAll();
	    	 modelDisplay.repaintLastChoice();
	    } else if(arg0.getSource() == this.maskRemove) {
	    	 modelDisplay.doMaskRemove();
	    	 modelDisplay.repaintLastChoice();
	    } else if(arg0.getSource() == this.maskInvert) {
	    	 modelDisplay.doMaskInvert();
	    	 modelDisplay.repaintLastChoice();
	    } else if(arg0.getSource() == this.maskSelectByDepth) {
	    	try {
		    	 float dep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "Depth to select:",
							"Create mask for certain depth", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
		    	 if ((dep > 0)) {
		    		 modelDisplay.doSelectMaskBy(dep);
			    	 modelDisplay.repaintLastChoice();
		    	 }
	    	} catch (Exception ex) {
	    		log(ex.toString(),LogLevel.debug);
	    	} // try / catch
	    } else if(arg0.getSource() == this.maskSelectByRange) {
	    	try {
		    	 float sdep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "select start depth:",
							"Create mask for depth range", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
		    	 if ((sdep > 0)) {
		    		 float edep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "select end depth:",
								"Create mask for depth range", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
		    		 if ((edep > 0)) {
		    			 modelDisplay.doSelectMaskByRange(sdep,edep);
		    			 modelDisplay.repaintLastChoice();
		    		 }
		    	 }
	    	} catch (Exception ex) {
	    		log(ex.toString(),LogLevel.debug);
	    	} // try / catch
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
	    } else if(arg0.getSource() == this.maskSetDepthSmooth) {
	    	if (modelDisplay.isMasked()) {
		    	try {
			    	 float dep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "Depth to set:",
								"Setting a fixed depth for masked area", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
			    	 if ((dep > 0)) {
			    		 modelDisplay.doMaskSetDepthSmooth(dep);
				    	 modelDisplay.repaintLastChoice();
			    	 }
		    	} catch (Exception ex) {
		    		log(ex.toString(),LogLevel.debug);
		    	} // try / catch
	    	} // if (modelDisplay.isMasked())
	    } else if(arg0.getSource() == this.maskSetHemisphere) {
	    	if (modelDisplay.isMasked()) {
	    		modelDisplay.doHemisphere(false);
	    		modelDisplay.repaintLastChoice();
	    	}
	    } else if(arg0.getSource() == this.maskSetHemisphereConcave) {
	    	if (modelDisplay.isMasked()) {
	    		modelDisplay.doHemisphere(true);
	    		modelDisplay.repaintLastChoice();
	    	}
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
	    } else if(arg0.getSource() == this.maskRiseLower) {
	    	if (modelDisplay.isMasked()) {
	    		float liftVal = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "+value = more depth, -value = less depth",
						"rise or lower depth in masked area", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
	    		if ((liftVal != 0)) {
	    			modelDisplay.doLift(liftVal);
		    		modelDisplay.repaintLastChoice();	
	    		}
	    	}
	    } else if(arg0.getSource() == this.maskGradientUpwards) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientUpwards);
	    } else if(arg0.getSource() == this.maskGradientUpwardsToRight) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientUpwardsToRight);
	    } else if(arg0.getSource() == this.maskGradientLeftToRight) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientLeftToRight);
	    } else if(arg0.getSource() == this.maskGradientDownwardsToRight) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientDownwardsToRight);
	    } else if(arg0.getSource() == this.maskGradientDownwards) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientDownwards);
	    } else if(arg0.getSource() == this.maskGradientDownwardsToLeft) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientDownwardsToLeft);
	    } else if(arg0.getSource() == this.maskGradientRightToLeft) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientRightToLeft);
	    } else if(arg0.getSource() == this.maskGradientUpwardsToLeft) {
	    	if (modelDisplay.isMasked()) doMaskGradient(this.maskGradientUpwardsToLeft);
	    }
	}
	

	private void doMaskGradient(JMenuItem maskGradientDegreeItem) {
		float sDep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "Start depth:",
				"At which depth should the gradient start?", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
		if (sDep > 0) {
			float eDep = Float.parseFloat((String)JOptionPane.showInputDialog(mainFrame, "End depth:",
					"At which depth should the gradient end?", JOptionPane.QUESTION_MESSAGE, null, null, modelDisplay.getRememberedFloat()));
			if (eDep > 0) {
				if (maskGradientDegreeItem == this.maskGradientUpwards) modelDisplay.doGradient(sDep,eDep,0);
				if (maskGradientDegreeItem == this.maskGradientUpwardsToRight) modelDisplay.doGradient(sDep,eDep,45);
				if (maskGradientDegreeItem == this.maskGradientLeftToRight) modelDisplay.doGradient(sDep,eDep,90);
				if (maskGradientDegreeItem == this.maskGradientDownwardsToRight) modelDisplay.doGradient(sDep,eDep,135);
				if (maskGradientDegreeItem == this.maskGradientDownwards) modelDisplay.doGradient(sDep,eDep,180);
				if (maskGradientDegreeItem == this.maskGradientDownwardsToLeft) modelDisplay.doGradient(sDep,eDep,225);
				if (maskGradientDegreeItem == this.maskGradientRightToLeft) modelDisplay.doGradient(sDep,eDep,270);
				if (maskGradientDegreeItem == this.maskGradientUpwardsToLeft) modelDisplay.doGradient(sDep,eDep,315);
						
				modelDisplay.repaintLastChoice();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static String getValidBearerToken() throws Exception {
		
		SeeneAPI api = new SeeneAPI(pd);
		
		// Step 1 - try to read bearer token from config file
		seeneBearerToken = getParameterFromConfiguration(configFile, "api_token");
		
		// Step 2 - test the stored bearer token
		if ((seeneBearerToken!=null) && (seeneBearerToken.length() > 0)) {
			if (testBearerToken(seeneBearerToken)) {
				log("BEARER: stored bearer token still valid!", LogLevel.debug);
				return seeneBearerToken;
			} else { // Step 3 - if test fails, try to refresh bearer token
				log("BEARER: bearer token expired! trying to get new one...", LogLevel.debug);
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
				log("BEARER: no bearer token found. Trying to request bearer token!", LogLevel.debug);
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
	private static Boolean testBearerToken(String token) {

		SeeneAPI api = new SeeneAPI(pd);
		
		try {
			String userID = api.requestUserIDfromOldAPI(seeneUser);
			log("Bearer-Test: userID " + userID,LogLevel.debug);
			Map response = api.requestUserInfo(userID, token);
			String username = (String)response.get("username");
			log("Bearer-Test: username for ID " + username,LogLevel.debug);
			if (username.equalsIgnoreCase(seeneUser)) return true;
		} catch (Exception e) {
			log("Bearer-Test failed: " + e.getMessage(),LogLevel.warn);
		}
		
		return false; 
	}
	
	
	private static void insertOrReplaceConfigParameter(File cf, String param, String newValue) {
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
			    		  if (confirmedOverwrite("Seene")) {
			    			  openSeene(seeneFolder);
			    			  showButtonInfo();
			    		  }
			    	  } // isDirectory
			      } // exists
			} // not null
		} // double click
		
	}
	
	
	private Boolean confirmedOverwrite(String kindTerm) {
		if (!currentHasChanges) return true;
		int an = JOptionPane.showConfirmDialog(mainFrame,
			    "Thera are changes in the editor!\n"
			    + "if you load/reload now the changes will be lost!\n"
        		+ "Press YES to load the " + kindTerm + " anyway.\n"
        		+ "Press NO to save your changes first.",
			    "CAUTION: Changes will be lost!",
			    JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
		if (an == JOptionPane.YES_OPTION) return true;
		return false;
	}
	
	private void openSeene(File seeneFolder) {

		currentSeene = new SeeneObject(seeneFolder);
		tbLoadMaskCombo.removeAllItems(); 
		
		String seenePath = currentSeene.getLocalpath();
		String offlinePath = storage.getOfflineDir().getAbsolutePath();
		
		// Set Localname if Seene is loaded from the "Offline" pool
		if (seenePath.toLowerCase().contains(offlinePath.toLowerCase())) {
			currentSeene.setLocalname(seenePath.substring(offlinePath.length()+1));
			// Trying to find stored masks
			reloadMaskLoadCombo(seeneFolder);
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
			log("Model min: " +  currentSeene.getModel().getMinFloat(),LogLevel.info);
			log("Model max: " +  currentSeene.getModel().getMaxFloat(),LogLevel.info);
			normalizer.setNormMaxFloat(currentSeene.getModel().getMaxFloat());
			normalizer.setNormMinFloat(currentSeene.getModel().getMinFloat());
			modelDisplay.setModel(currentSeene.getModel());
			modelDisplay.setPoster(currentSeene.getPoster());
		}
		
	}
	
	private void reloadMaskLoadCombo(File seeneFolder) {
		File masksDir = new File(seeneFolder.getAbsolutePath() + File.separator + "Masks");
		tbLoadMaskCombo.removeAllItems(); 
		tbLoadMaskCombo.addItem(new String("- select a mask -"));
		int cnt = 0;
		if (masksDir.exists()) {
			File[] files = masksDir.listFiles();
			Arrays.sort(files);
			if (files != null) {
				for (int i = files.length - 1; i > -1; i--) {
					log(files[i].getAbsolutePath(),LogLevel.debug);
					if ((!files[i].isDirectory()) && (files[i].getName().endsWith(".mask"))) {
						tbLoadMaskCombo.addItem(files[i].getName().replaceFirst("[.][^.]+$", ""));
						cnt++;
					} // directory
				} // for
			} // if (files != null)
		} // if (masksDir.exists())
		if (cnt == 0) tbLoadMaskCombo.removeAllItems(); 
	}
	

	@SuppressWarnings("serial")
	class ModelGraphics extends Canvas {
		
		public SeeneObject seeneObject;
		public SeeneModel model; 
		public SeeneTexture poster;
		private List<Boolean> mask = new ArrayList<Boolean>();
		private int undomax = STK.UNDO_RINGBUFFER_SIZE;
		private List<Boolean>[] undomask = (ArrayList<Boolean>[])new ArrayList[undomax];
		private List<Float>[] undomaps = (ArrayList<Float>[])new ArrayList[undomax];
		private String[] undotext = new String[undomax];
		private List<Boolean>[] redomask = (ArrayList<Boolean>[])new ArrayList[undomax];
		private List<Float>[] redomaps = (ArrayList<Float>[])new ArrayList[undomax];
		private String[] redotext = new String[undomax];
		private int undoStep = 0;
		private int undoOld = 0;
		private int redoStep = 0;
		private int redoOld = 0;
		int maskBrushRadius = 2;
		public String lastChoice = "";
		float rememberedFloat = STK.INIT_DEPTH;

		public int canvasSize=STK.WORK_WIDTH;
		public int pointSize=2;
		public boolean inverted=true;
		boolean seeneLike=false;
		int last_my = 0; // last mouse Y position for 3D plot
		
		public ModelGraphics(){
			setSize(canvasSize*getPointSize(), canvasSize*getPointSize());	
	        setBackground(Color.white);
	        
	        // MouseWheel to change size of the mask brush
	        addMouseWheelListener(new MouseWheelListener() {
				public void mouseWheelMoved(MouseWheelEvent e) {
					if ((lastChoice=="model") || (lastChoice=="poster")) {
						int steps = e.getWheelRotation();
						maskBrushRadius += steps;
						if (maskBrushRadius<0) maskBrushRadius=0;
						if (maskBrushRadius>8) maskBrushRadius=8;
						log("new Mask Brush Radius: " + maskBrushRadius,LogLevel.debug);
						setMaskCursorWithSize(maskBrushRadius);
					}
				}
			});
	        
	        // MouseListener for Painting the Mask
	        addMouseListener(new MouseAdapter(){
	        	int w = 0;
	        	int h = 0;
	        	int ndx = 0;
	        	int mOffX = 0;
	        	int mOffY = 0;
	        	int downX = 0;
	        	int downY = 0;
	        	int last_n = 0;

	        	BufferedImage textureTransformed = null;
	        	Boolean maskPaintMode;
	        	
	        	volatile private boolean mouseDown = false;
	        	
	        	public void mouseEntered(MouseEvent e) {
	        		if ((lastChoice=="model") || (lastChoice=="poster")) setMaskCursorWithSize(maskBrushRadius);
	        		if (lastChoice=="3D") SetUpDownCursor();
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
	        	        	downX = e.getX();
	        	        	downY = e.getY();
	        	        	mOffX = MouseInfo.getPointerInfo().getLocation().x - downX;
	        	        	mOffY = MouseInfo.getPointerInfo().getLocation().y - downY;
	        	        	// DepthPoint Info
	        	        	if (e.getButton() != MouseEvent.BUTTON2) {
	        	        		if ((lastChoice=="model") || (lastChoice=="poster")) initMaskPaintThread();
	        	        		if (lastChoice=="3D") {
	        	        			if (e.getButton() == MouseEvent.BUTTON3) seeneLike=!seeneLike;
	        	        			getGraphics().setColor(Color.BLACK);
	        	        	    	getGraphics().fillRect(0, 0, canvasSize*getPointSize(), canvasSize*getPointSize());
	        	        	    	Image texture = poster.getTextureImage();
	        	            		int new_width = canvasSize*getPointSize();
	        	            		int new_height = canvasSize*getPointSize();
	        	        	    	textureTransformed = Helper.rotateAndResizeImage(texture, new_width/2, new_height/2, 90);
	        	        	    	drawPointCloud3D(getGraphics(),textureTransformed,model,1,last_my,last_my/2,seeneLike);
	        	        			init3DCloudRotationThread();
	        	        		}
	        	        	} else {
	        	        		if ((lastChoice=="model") || (lastChoice=="poster")) {
			        	        	float max = model.getMaxFloat();
			        	        	int mx = w - e.getX() / pointSize - 1;
		    						int my = e.getY() / pointSize;
		    						int n = positionFromCoords(mx, my);
		    						float f = model.getFloats().get(n);
		    						float cf = floatGreyScale(f, max);
			        	        	log("\nPOSITION: x: " + mx +  " - y: " + my + " (float number: " + n + ")\nCOLOR: " + cf + "\nDEPTH: " + f,LogLevel.info);
			        	        	setRememberedFloat(f);
	        	        		}
	        	        	}
	        	        }
	        	    }
	        	}
	        	
	        	public void mouseReleased(MouseEvent e) {
	        		if (model!=null) {
		        		if ((e.getButton() == MouseEvent.BUTTON1) || (e.getButton() == MouseEvent.BUTTON3)) {
		        	        mouseDown = false;
		        	        if (lastChoice=="model") repaintModelOnly();
		        	        if (lastChoice=="poster") repaintPosterOnly();
		        	    }
		        		if ((lastChoice=="model") || (lastChoice=="poster")) {
		        			if (e.getButton() == MouseEvent.BUTTON1) saveUndoStep(mask, "mask paint", model.getFloats(), true);
		        			if (e.getButton() == MouseEvent.BUTTON3) saveUndoStep(mask, "mask rubber", model.getFloats(), true);
		        		}
	        		}
	        	}

	        	volatile private boolean isRunning = false;
	        	
	        	private synchronized boolean checkAndMark() {
	        	    if (isRunning) return false;
	        	    isRunning = true;
	        	    return true;
	        	}
	        	
	        	private void init3DCloudRotationThread() {
	        	    if (checkAndMark()) {
	        	        new Thread() {
	        	            public void run() {
	        	                do {
	        	                	//int mx = Math.abs((MouseInfo.getPointerInfo().getLocation().x - downX) - mOffX);
	        						int my = ((MouseInfo.getPointerInfo().getLocation().y - downY) - mOffY) + last_my;
	        						if (my<0) my = 0;
	        						if (my>90) my = 90;
	        						//if (mx>90) mx = 90;
	        						
	        						//if ((last_mx!=mx) || (last_my!=my)) {
	        						if (last_my!=my) {
	        							try {
	        								//System.out.println("my " + my);	
	        								getGraphics().setColor(Color.BLACK);
	        								getGraphics().fillRect(0, 0, canvasSize*getPointSize(), canvasSize*getPointSize());
	        								drawPointCloud3D(getGraphics(),textureTransformed,model,1,my,my/2,seeneLike);
											sleep(120);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
	        							//last_mx=mx;
	        							last_my=my;
	        						}
	        						
	        	                } while (mouseDown);
	        	                isRunning = false;
	        	            }
	        	        }.start();
	        	    }
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
	        							n = positionFromCoords(mx, my);
	        							if ((n >= 0) && ( n < ndx) && (my < h) && (my >= 0)) mask.set(n, maskPaintMode);
	        						}
	        						if (n!=last_n) {
	        							if (maskPaintMode) paintMaskPartially(getGraphics(), mx, my, maskBrushRadius);
	        							else repaintLastChoice();
	        							
	        							last_n=n;
	        						} 
	        						
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
	    	paintMaskCompletely(g);
	    }
	    
	    private void paintMaskCompletely(Graphics g) {
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
	    
	    // method to speed up mask drawing
	    private void paintMaskPartially(Graphics g, int mx, int my, int rad) {
	    	if (model!=null) {
		    	Color mc = new Color(1f,0f,0f,.4f );
		    	g.setColor(mc);
	    		
	    		int w = model.getDepthWidth();
		        int h = model.getDepthHeight();
		        int ndx = w * h;
		        int p = getPointSize();
		        int n;
		        
		        if (rad>0) {
					for (int bx=0-rad;bx<rad;bx++) {
						for (int by=0-rad;by<rad;by++) {
							n = (mx + bx) * w + (my + by);
							if ((n >= 0) && ( n < ndx) && ((my + by) < h) && ((my + by) >= 0)) g.fillRect((w-(mx+bx)-1)*p, (my+by)*p , p, p);
						}
					}
				} else {
					n = positionFromCoords(mx, my);
					if ((n >= 0) && ( n < ndx) && (my < h) && (my >= 0)) g.fillRect((w-mx-1)*p, my*p , p, p);
				}
		        
	    	}
	    }
	    
	    private int positionFromCoords(int x_pos, int y_pos) {
	    	return x_pos * model.getDepthWidth() + y_pos;
	    }
	    
	    private int yCoordFromPosition(int n){
	    	return n / model.getDepthWidth();
	    }
	    
	    private int xCoordFromPosition(int n){
	    	return n - (model.getDepthWidth() * yCoordFromPosition(n));
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
	    
	    private void paint3D(Graphics g, SeeneTexture poster, SeeneModel model) {
	    	g.setColor(Color.BLACK);
	    	g.fillRect(0, 0, canvasSize*getPointSize(), canvasSize*getPointSize());
	    	Image texture = poster.getTextureImage();
    		int new_width = canvasSize*getPointSize();
    		int new_height = canvasSize*getPointSize();
	    	BufferedImage textureTransformed = Helper.rotateAndResizeImage(texture, new_width/2, new_height/2, 90);
	
	    	if (model!=null) {
		        int step=1;
		        last_my=0;
		        drawPointCloud3D(g,textureTransformed,model,step,0,0,seeneLike);
	    	} // if (model!=null)
			
		}
	    
	    private void drawPointCloud3D(Graphics g, BufferedImage texture, SeeneModel model, int step, int degH, int degV, boolean seeneLikePow) {
	    	float f;
	        float max = model.getMaxFloat();
	        int w = model.getDepthWidth();
	        int h = model.getDepthHeight();
	        float cf;
        
	    	float stretch=1.6f;
	    	float parallel_offset=0.4f;
	    	int raise_factor=9;
	    	int offset=0;
	    	if (seeneLikePow) { raise_factor=13; offset=60; }
	    	int raise=0;
	    	
	    	for (int x=0;x<w;x+=step) {
	        	float ox=0.0f;	
	        	for (int y=0;y<h;y+=step) {
	        		f = model.getFloats().get(positionFromCoords(x, y));
	        		cf = floatGreyScale(f, max);
	        		Color newColor = colorFromTexture(texture, w-x-1, y);
	        		g.setColor(newColor);
	        		ox+=parallel_offset * step;
	        		double degH_rad = Math.toRadians(degH);
	        		double degV_rad = Math.toRadians(degV);
	        		int dx = Math.round((w-x-1) * stretch);
	        		int dy = Math.round(y * stretch);
	        		int rx = 240 - dx;
	        		int ry = 180 - dy;
	        		int wx = (int) (rx * Math.tan(degV_rad));
	        		int wy = (int) (ry * Math.sin(degH_rad));
	        		if (seeneLikePow) raise = Math.round(((1.0f-cf)*raise_factor)*((1.0f-cf)*raise_factor)) * -1;
	        		else raise = Math.round((cf*raise_factor)*(cf*raise_factor));
	        		g.fillRect(Math.round(ox) + dx + wx , dy + wy + raise + offset , 2, 2);
	        	} // for y
	        } //  for x
	    	g.setColor(Color.GREEN);
	    	if (seeneLikePow) g.drawString("Seene like exponentiation (right click to change)", 10, canvasSize*getPointSize()-10);
	    	if (!seeneLikePow) g.drawString("normal exponentiation (right click to change)", 10, canvasSize*getPointSize()-10);
	    }
	    
	    private Color colorFromTexture(BufferedImage texture, int x, int y) {
	    	  int  clr   =  texture.getRGB(x,y); 
	    	  int  red   = (clr & 0x00ff0000) >> 16;
	    	  int  green = (clr & 0x0000ff00) >> 8;
	    	  int  blue  =  clr & 0x000000ff;
	    	  return new Color(red,green,blue);
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
				this.paintMaskCompletely(g);
			}
	    }
	    
	    public void repaintModelOnly() {
	    	if (model!=null) {
	    		lastChoice="model";
				Graphics g = getGraphics();
				//setSize(model.getDepthWidth()*getPointSize(), model.getDepthHeight()*getPointSize());
				this.paintModel(g, model);
				this.paintMaskCompletely(g);
			}
	    }
	    
	    public void repaintPosterOnly() {
	    	if (poster!=null) {
	    		lastChoice="poster";
				Graphics g = getGraphics();
				this.paintPoster(g, poster);
				this.paintMaskCompletely(g);
			}
	    }
	    
	    public void repaint3DOnly() {
	    	if ((model!=null) && (poster!=null)) {
	    		lastChoice="3D";
				Graphics g = getGraphics();
				this.paint3D(g, poster, model);
	    	}
		}
	    
	    
		private void setMaskCursorWithSize(int radius) {
			//TODO recreate cursors as 32x32 pics!
			Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			Image image = toolkit.getImage(SeeneToolkit.class.getResource("/images/cursor" + radius + ".png"));
			Cursor cu = toolkit.createCustomCursor(image , new Point((radius + radius / 2) + 1, (radius + radius / 2) + 1), "");
			setCursor(cu);
		}
		
		private void SetUpDownCursor(){
			Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			Image image = toolkit.getImage(SeeneToolkit.class.getResource("/images/updown.png"));
			Cursor cu = toolkit.createCustomCursor(image , new Point(1,1),"");
			setCursor(cu);
		}
	    
	    public void initializeUndo() {
	    	undoStep = 0;
	    	redoStep = 0;
	    	undoOld = 0;
	    	redoOld = 0;
	    	for (int n=0;n<undomax;n++) {
	    		undomask[n] = null;
	    		undomaps[n] = null;
	    		undotext[n] = null;
	    	}
	    	mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    	undomask[0] = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    	undomaps[0] = new ArrayList<Float>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), STK.INIT_DEPTH));
	    	for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
				undomaps[0].set(n, model.getFloats().get(n));;
			}
	    	undotext[0] = "-origin-";
	    	
	    	maskUndo.setText("undo");
	    	maskRedo.setText("redo");
			maskUndo.setEnabled(false);
			maskRedo.setEnabled(false);
			currentHasChanges = false;
		}
	    
	    
	    private void saveUndoStep(List<Boolean> mask, String doing, List<Float> depthmap, boolean resetRedo) {
	    	
	    	if (resetRedo) {
	    		redoStep = 0;
	    		redoOld =0;
	    		maskRedo.setText("redo");
	    		maskRedo.setEnabled(false);
	    	}

	    	undoStep++;
	    	
    		if (undoStep == undomax) {
    			undoStep = 0;
    			undoOld = 1;
    		}
    		
    		if (undoOld != 0) undoOld = undoStep + 1;
    		if (undoOld == undomax) undoOld = 0;
    		
    		// store the depthmap
    		if ((depthmap!=null) && (!depthmap.isEmpty())) {
    			// Initialize depthmap storage
    			undomaps[undoStep] = new ArrayList<Float>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), STK.INIT_DEPTH));
    			// copy value-by-value to loose byRef
    			for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
    				undomaps[undoStep].set(n, depthmap.get(n));;
    			}
    		}
	    	
    		// store the mask
	    	if ((mask!=null) && (!mask.isEmpty())) {
	    		// Initialize mask storage
	    		undomask[undoStep] = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		// copy value-by-value to loose byRef
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			undomask[undoStep].set(n, mask.get(n));
	    		}
	    	}

	    	undotext[undoStep] = doing;
	    	maskUndo.setText("undo (" + doing + ")");
    		maskUndo.setEnabled(true);
    		currentHasChanges = true;
		}
	    
	    private void saveRedoStep(List<Boolean> mask, String doing, List<Float> depthmap) {
	    	
	    	// store the depthmap
    		if ((depthmap!=null) && (!depthmap.isEmpty())) {
    			// Initialize depthmap storage
    			redomaps[redoStep] = new ArrayList<Float>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), STK.INIT_DEPTH));
    			// copy value-by-value to loose byRef
    			for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
    				redomaps[redoStep].set(n, depthmap.get(n));;
    			}
    		}
	    	
    		// store the mask
	    	if ((mask!=null) && (!mask.isEmpty())) {
	    		// Initialize mask storage
	    		redomask[redoStep] = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		// copy value-by-value to loose byRef
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			redomask[redoStep].set(n, mask.get(n));
	    		}
	    	}
	    		
	    	redotext[redoStep] = doing;
	    	maskRedo.setText("redo (" + doing + ")");
    		maskRedo.setEnabled(true);
	    	
	    	redoStep++;
	    	
    		if (redoStep == undomax) {
    			redoStep = 0;
    			redoOld = 1;
    		}
    		
    		if (redoOld != 0) redoOld = redoStep + 1;
    		if (redoOld == undomax) redoOld = 0;
	    }
	    
	    
	    public void doUndo() {
	    	
	    	saveRedoStep(undomask[undoStep], undotext[undoStep], undomaps[undoStep]);
	    		    	
	    	int undoNow = undoStep - 1;
	    	if (undoNow < 0) undoNow = undomax -1;

	    	model.setFloats(undomaps[undoNow]);
	    	
	    	for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
    			mask.set(n, undomask[undoNow].get(n));
    			model.getFloats().set(n, undomaps[undoNow].get(n));
    		}
	    	model=findModelExtema(model);
			repaintLastChoice();
			
			if (undoNow == undoOld) {
	    		maskUndo.setText("undo");
				maskUndo.setEnabled(false);
	    	} else {
	    		maskUndo.setText("undo (" + undotext[undoNow] + ")");
	    		maskUndo.setEnabled(true);
	    	}
			
			undoStep--;
			if (undoStep < 0) undoStep = undomax -1;
		}
	    

	    public void doRedo() {
			
	    	int redoNow = redoStep - 1;
	    	if (redoNow < 0) redoNow = undomax -1;
	    	
	    	saveUndoStep(redomask[redoNow], redotext[redoNow], redomaps[redoNow], false);
	    	
	    	for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
    			mask.set(n, redomask[redoNow].get(n));
    			model.getFloats().set(n, redomaps[redoNow].get(n));
    		}
	    	model=findModelExtema(model);
			repaintLastChoice();
			
			if (redoNow == redoOld) {
	    		maskRedo.setText("redo");
				maskRedo.setEnabled(false);
	    	} else {
	    		int redoNext = redoNow - 1;
	    		if (redoNext < 0) redoNext = undomax - 1;
	    		maskRedo.setText("redo (" + redotext[redoNext] + ")");
	    		maskRedo.setEnabled(true);
	    	}
			
			redoStep--;
			if (redoStep < 0) redoStep = undomax -1;
			
			repaintLastChoice();
		}
	    
	    
	    // Mask Operation Methods
	    public void doMaskAll() {
	    	if (model!=null) {
	    		mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), true));
	    		saveUndoStep(mask,"mask all", model.getFloats(), true);
	    	}
	    }
	    
	    public void doMaskRemove() {
	    	doMaskRemove(true);
	    }
	    
		private void doMaskRemove(Boolean saveUndo) {
	    	if (model!=null) {
	    		mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		if (saveUndo) saveUndoStep(mask, "remove mask", model.getFloats(), true);
	    	}
	    }
	    
	    public void doMaskInvert() {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			mask.set(n, !mask.get(n));
	    		}
	    		saveUndoStep(mask, "invert mask", model.getFloats(), true);
	    	}
	    }
	    
	    public void doSelectMaskBy(float dep) {
	    	if (model!=null) {
	    		mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (model.getFloats().get(n)==dep) mask.set(n, true);
	    		}
	    		saveUndoStep(mask, "select by value " + dep, model.getFloats(), true);
	    	}
		}
	    
	    public void doSelectMaskByRange(float sdep, float edep) {
	    	if (model!=null) {
	    		mask = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if ((model.getFloats().get(n)>=sdep) && (model.getFloats().get(n)<=edep))  mask.set(n, true);
	    		}
	    		saveUndoStep(mask, "select by range " + sdep + " - " + edep, model.getFloats(), true);
	    	}
		}
	    
	    public void doMaskSetDepth(float dep) {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) model.getFloats().set(n, dep);
	    		}
	    		model=findModelExtema(model);
	    		saveUndoStep(mask, "set depth to " + dep, model.getFloats(), true);
	    	}
		}
	    
	    public void doMaskSetDepthSmooth(float dep) {
	    	if (model!=null) {
	    		List<Boolean> maskInsideBorder = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		List<Boolean> maskOutsideBorder = new ArrayList<Boolean>(Collections.nCopies(model.getDepthWidth()*model.getDepthHeight(), false));
	    		// run #1 find and mark borders of mask
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) markBorders(xCoordFromPosition(n), yCoordFromPosition(n), maskInsideBorder, maskOutsideBorder);
	    		}
	    		// run #2 set smooth transition depths for border areas
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if ((mask.get(n)) && (!maskInsideBorder.get(n))) model.getFloats().set(n, dep);
	    			if (maskOutsideBorder.get(n)) {
	    				float out_val = model.getFloats().get(n);
	    				float stepd = (dep - out_val) / 3;
	    				model.getFloats().set(n, out_val + stepd);
	    				setInsideBorderDepth(xCoordFromPosition(n), yCoordFromPosition(n),dep - stepd, maskInsideBorder);
	    			}
	    		}
	    		model=findModelExtema(model);
	    		saveUndoStep(mask, "set depth smooth " + dep, model.getFloats(), true);
	    	}
		}
	    
	    // give x to y and y to x, because depthmap is rotated 90 degrees
	    private void setInsideBorderDepth(int y, int x, float depth, List<Boolean> iB) {
	    	if ((x-1 >= 0) && (iB.get(positionFromCoords(x-1, y)))) model.getFloats().set(positionFromCoords(x-1, y), depth);
	    	if ((x+1 < model.getDepthWidth()) && (iB.get(positionFromCoords(x+1, y)))) model.getFloats().set(positionFromCoords(x+1, y), depth);
	    	if ((y-1 >= 0) && (iB.get(positionFromCoords(x, y-1)))) model.getFloats().set(positionFromCoords(x, y-1), depth);
	    	if ((y+1 < model.getDepthHeight()) && (iB.get(positionFromCoords(x, y+1)))) model.getFloats().set(positionFromCoords(x, y+1), depth);
		}

	    // give x to y and y to x, because depthmap is rotated 90 degrees
		public void  markBorders(int y, int x, List<Boolean> iB, List<Boolean> oB) {
	    	if ((x-1 >= 0) && (!mask.get(positionFromCoords(x-1, y)))) {
	    		iB.set(positionFromCoords(x, y), true);
	    		oB.set(positionFromCoords(x-1, y), true);
	    	}
	    	if ((x+1 < model.getDepthWidth()) && (!mask.get(positionFromCoords(x+1, y)))) {
	    		iB.set(positionFromCoords(x, y), true);
	    		oB.set(positionFromCoords(x+1, y), true);
	    	}
	    	if ((y-1 >= 0) && (!mask.get(positionFromCoords(x, y-1)))) {
	    		iB.set(positionFromCoords(x, y), true);
	    		oB.set(positionFromCoords(x, y-1), true);
	    	}
	    	if ((y+1 < model.getDepthHeight()) && (!mask.get(positionFromCoords(x, y+1)))) {
	    		iB.set(positionFromCoords(x, y), true);
	    		oB.set(positionFromCoords(x, y+1), true);
	    	}
	    }

		public void doLift(float liftVal) {
			if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) {
	    				float newDepth = model.getFloats().get(n) + liftVal;
	    				if (newDepth < 0 ) newDepth = 0.0f;
	    				model.getFloats().set(n, newDepth);
	    			}
	    		}
	    		model=findModelExtema(model);
	    		saveUndoStep(mask, "lift depth " + liftVal, model.getFloats(), true);
	    	}
		}
		
		@SuppressWarnings("unused")
		public void doHemisphere(boolean concave) {
			if (model!=null) {
				int x_max = 0;
				int x_min = model.getDepthWidth();
				int y_max = 0;
				int y_min = model.getDepthHeight();
				for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) {
	    				if (xCoordFromPosition(n) > x_max) x_max = xCoordFromPosition(n);
	    				if (xCoordFromPosition(n) < x_min) x_min = xCoordFromPosition(n);
	    				if (yCoordFromPosition(n) > y_max) y_max = yCoordFromPosition(n);
	    				if (yCoordFromPosition(n) < y_min) y_min = yCoordFromPosition(n);
	    			}
	    		}
				int xr = (x_max - x_min) / 2;
				int yr = (y_max - y_min) / 2;
				int hr = (xr + yr) / 2;
				int xm = x_min + xr;
				int ym = y_min + yr;
				//System.out.println("xr: " + xr + " - yr: " + yr + " - hr: " + hr + " - xm: " + xm + " - ym: " + ym);
				int x,y,hx,hy;
				double c,d;
				float hd;
				for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
					if (mask.get(n)) {
						x = xCoordFromPosition(n);
						y = yCoordFromPosition(n);
						hx = x - xm;
						hy = y - ym;
						c = Math.sqrt((hx*hx)+(hy*hy));
						if (c<=hr) {
							d = Math.sqrt((hr*hr)-(c*c)) / 240; 
							//System.out.println("x: " + x + " - y: " + y + " - hx: " + hx + " - hy: " + hy + " - c: " + c + " - d: " + d );
							if (concave) hd = (float) (model.getFloats().get(n) + d);
							else  hd = (float) (model.getFloats().get(n) - d);	
							if (hd<0) hd=0;
							model.getFloats().set(n, hd);
						}
					}
				}
				model=findModelExtema(model);
				if (concave) saveUndoStep(mask, "hemisphere concave", model.getFloats(), true);
				else saveUndoStep(mask, "hemisphere convex", model.getFloats(), true);
					
			}
		}
	    
	    public void doDivideBy(float div) {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) model.getFloats().set(n, model.getFloats().get(n) / div);
	    		}
	    		model=findModelExtema(model);
	    		saveUndoStep(mask, "divide by " + div, model.getFloats(), true);
	    	}
		}
	    
	    public void doGradient(float sDep, float eDep, int d) {
	    	if (model!=null) {
	    		int w = model.getDepthWidth();
	        	int h = model.getDepthHeight();
	        	int ndx = w * h;
	        	int x = 0, y = 0;
	        	int max_x = 0, min_x = w, max_y = 0, min_y = h;
	        	
	    		for (int n = 0; n < ndx; n++) {
	    			if (mask.get(n)) {
	    				y = n / w;
	    				x = n - (w * y);
	    				if (x > max_x) max_x = x;
	    				if (x < min_x) min_x = x;
	    				if (y > max_y) max_y = y;
	    				if (y < min_y) min_y = y;
	    				//System.out.println("x: " + x + " - y:" + y);
	    			}
	    		}
	    		System.out.println("max x: " + max_x + " - min x:" + min_x);
	    		System.out.println("max y: " + max_y + " - min y:" + min_y);
	    		
	    		float dDiff = eDep - sDep;
	    		int lxDiff = max_x - min_x;
	    		int lyDiff = max_y - min_y;
	    		
	    		float stx = dDiff / lxDiff;
	    		float sty = dDiff / lyDiff;
	    		
	    		System.out.println("depthdifference: " + dDiff + " - distance X: " + lxDiff + " - step X: " + stx);
	    		System.out.println("depthdifference: " + dDiff + " - distance Y: " + lyDiff + " - step Y: " + sty);
	    		
	    		float newDepth = 0.0f;
	    		
	    		for (int n = 0; n < ndx; n++) {
	    			if (mask.get(n)) {
	    				y = n / w;
	    				x = n - (w * y);
	    				
	    				if (d ==   0) newDepth = sDep + ((max_x - x) * stx); 	// upwards			= 0�
	    				if (d ==  90) newDepth = sDep + ((max_y - y) * sty); 	// left to right    = 90�
	    				if (d == 180) newDepth = sDep + ((x - min_x) * stx); 	// downwards		= 180�
	    				if (d == 270) newDepth = sDep + ((y - min_y) * sty); 	// right to left	= 270�
	    				if (d ==  45) newDepth = sDep + ((((max_y - y) * sty) + ((max_x - x) * stx)) / 2); // upwards to right		= 45�
	    				if (d == 135) newDepth = sDep + ((((max_y - y) * sty) + ((x - min_x) * stx)) / 2); // downwards to right	= 135�
	    				if (d == 225) newDepth = sDep + ((((y - min_y) * sty) + ((x - min_x) * stx)) / 2); // downwards to left		= 225�
	    				if (d == 315) newDepth = sDep + ((((y - min_y) * sty) + ((max_x - x) * stx)) / 2); // upwards to left		= 315� 
	    				
	    				model.getFloats().set(n, newDepth);
	    			}
	    		}
	    		
	    		model=findModelExtema(model);
	    		saveUndoStep(mask, "gradient " + d + "�", model.getFloats(), true);
	    		
	    	} //if (model!=null)
		}
	    
	    public Boolean isMasked() {
	    	if (model!=null) {
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) return true;
	    		}
	    	}
	    	return false;
		}
	    
	    public void saveMask(File maskFile) {
	    	try {
	    		PrintWriter writer = new PrintWriter(maskFile);
	    		for (int n = 0; n < model.getDepthWidth()*model.getDepthHeight(); n++) {
	    			if (mask.get(n)) writer.print("1");
	    			else writer.print("0");
	    		} // for
	    		writer.close();
	    	} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} //try/catch
	    }
	    
	    public void loadMask(File maskFile) {
	       log("Loading Mask: " + maskFile.getAbsolutePath(),LogLevel.info);
	       try {
	    	   FileReader f = new FileReader(maskFile);
	    	   int c;
	    	   int n = 0;
	    	   while((c = f.read()) != -1) { 
	    		   if ((char)c == '0') mask.set(n, false);
	    		   else mask.set(n, true);
	    		   n++;
	    	   }
	    	   f.close();
	    	   repaintLastChoice();
	    	   repaintMaskOnly();
	       } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	       }
	    }
	    
	    // Getter and Setter
	    public SeeneObject getSeeneObject() {
			return seeneObject;
		}
	    public void setSeeneObjectWithUndo(SeeneObject seeneObject, String undoText) {
			this.seeneObject = seeneObject;
			setModel(seeneObject.getModel(),false);
			setPoster(seeneObject.getPoster());
			repaintLastChoice();
			saveUndoStep(mask, undoText, seeneObject.getModel().getFloats(), false);
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
	    public void setModel(SeeneModel model, Boolean undoInitialization) {
	    	this.model = model;
			doMaskRemove(false);
			if (undoInitialization) initializeUndo();
			repaintModelOnly();
	    }
		public void setModel(SeeneModel model) {
			setModel(model,true);
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
    	
    	String caption = currentSeene.getCaption();
    	if ((caption==null) || (caption.length()==0)) caption = STK.DEFAULT_CAPTION;
    	JTextArea tfCaption = new JTextArea(caption);
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
	
	private static void showAuthorizationDialog() {
		
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
    					System.setProperty("http.proxyHost", line.substring(11));
    					System.setProperty("https.proxyHost", line.substring(11));
    					System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
    					pd.setHost(line.substring(11));
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.port="))) {
    					log("configured proxy.port: " + line.substring(11),LogLevel.debug);
    					System.setProperty("http.proxyPort", line.substring(11));
    					System.setProperty("https.proxyPort", line.substring(11));
    					pd.setPort(line.substring(11));
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.user="))) {
    					log("configured proxy.user: " + line.substring(11),LogLevel.debug);
    					System.setProperty("http.proxyUser", line.substring(11));
    					pd.setUser(line.substring(11));
    				}
    				if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("proxy.pass="))) {
    					log("configured proxy.pass: " + line.substring(11),LogLevel.debug);
    					System.setProperty("http.proxyPassword",line.substring(11));
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

	private void showButtonInfo() {
		Graphics g = modelDisplay.getGraphics();
		  g.setColor(Color.GREEN);
		  g.drawString("buttons: left = draw mask / right = erase mask / middle = info / wheel: change brush", 10, modelDisplay.getHeight()-8);
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

