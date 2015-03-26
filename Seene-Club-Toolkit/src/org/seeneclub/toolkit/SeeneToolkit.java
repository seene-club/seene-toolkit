package org.seeneclub.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class SeeneToolkit implements Runnable, ActionListener, MouseListener {
	
	JFrame mainFrame = new JFrame("...::: Seene-Club-Toolkit-GUI :::...");
	
	// Task Menu Items
    JMenuItem taskBackupPublic = new JMenuItem("backup public seenes");
    JMenuItem taskBackupPrivate = new JMenuItem("backup private seenes");
	
	// method main - all begins with a thread!
	public static void main(String[] args) {
		new Thread(new SeeneToolkit()).start();
	}

	@Override
	public void run() {
		
		mainFrame.setSize(1024,768);
		
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	 System.exit(0);
		    }
		});
		
		// menu bar
        JMenuBar bar = new JMenuBar();
        JMenu filemenu = new JMenu("Seene-Club");
        
        JMenuItem itemSettings = new JMenuItem("Settings");
        itemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                //showSettingsDialog();
            }
        });
        
        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });
        
        filemenu.add(itemSettings);
        filemenu.add(itemExit);
        
        JMenu taskmenu = new JMenu("Tasks");
        
        taskBackupPublic.addActionListener(this);
        taskBackupPrivate.addActionListener(this);
                
        taskmenu.add(taskBackupPublic);
        taskmenu.add(taskBackupPrivate);
        
        bar.add(filemenu);
        bar.add(taskmenu);
        
        mainFrame.setJMenuBar(bar);

		mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
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

}
