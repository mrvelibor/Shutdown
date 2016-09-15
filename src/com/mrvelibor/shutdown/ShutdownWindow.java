package com.mrvelibor.shutdown;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

public class ShutdownWindow extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	private final JRadioButton mTimeButton, mDateButton;
	private final ShutdownPanel mTimePanel, mDatePanel;
	private final JButton mButton;
	private final JLabel mTimerLabel;
	
	public ShutdownWindow() {
		super("Shutdown");
		getRootPane().setBorder(new EmptyBorder(0, 0, 5, 0));
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		/* LABELS */
		mTimerLabel = new JLabel("timer off", JLabel.CENTER);
		mTimerLabel.setFont(mTimerLabel.getFont().deriveFont(Font.BOLD, 21f));
		c.gridwidth = 4;
		add(mTimerLabel, c);
		
		/* SELECTORS */
		c.gridwidth = 1;
		
		ButtonGroup group = new ButtonGroup();
		
		mTimeButton = new JRadioButton("Shutdown in:", true);
		mTimeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mTimePanel.setEnabled(mTimeButton.isSelected());
			}
		});
		group.add(mTimeButton);
		c.gridy = 1;
		add(mTimeButton, c);
		
		mDateButton = new JRadioButton("Shutdown at:");
		mDateButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mDatePanel.setEnabled(mDateButton.isSelected());
			}
		});
		group.add(mDateButton);
		c.gridy = 2;
		add(mDateButton, c);
		
		/* PANELS */
		c.gridx = 1;
		c.gridwidth = 3;
		
		mTimePanel = new TimePanel();
		c.gridy = 1;
		add(mTimePanel, c);
		
		mDatePanel = new DatePanel();
		mDatePanel.setEnabled(false);
		c.gridy = 2;
		add(mDatePanel, c);
		
		/* BUTTONS */
		mButton = new JButton("Set");
		mButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mActivated) {
					Date time = mTimeButton.isSelected() ? mTimePanel.getShutdownTime() : mDatePanel.getShutdownTime();
					if(time == null) return;
					
					Calendar now = Calendar.getInstance();
					now.add(Calendar.SECOND, 10);
					if(time.before(now.getTime())) {
						int choice = JOptionPane.showConfirmDialog(ShutdownWindow.this,
								"Your computer will shut down immediatly.\nPlease save all your work before proceeding.", "Shutdown",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
						switch(choice)
						{
							case JOptionPane.CANCEL_OPTION:
							case JOptionPane.CLOSED_OPTION:
								return;
						}
					}
					
					shutdownAt(time);
				}
				else {
					cancelShutdown();
				}
			}
		});
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 3;
		add(mButton, c);
		
		/* MENU */
		JMenuBar menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem menuItem;
		
		menu = new JMenu("Options");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Æao");
		menuItem.setEnabled(false);
		menu.add(menuItem);
		
		menu = new JMenu("Help");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Troubleshoot");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String testVersion = "1.7.0_45-b18", activeVersion = System.getProperty("java.runtime.version");
				
				int comp = activeVersion.compareTo(testVersion), status;
				if(comp > 0) status = JOptionPane.QUESTION_MESSAGE;
				else if(comp < 0) status = JOptionPane.ERROR_MESSAGE;
				else status = JOptionPane.INFORMATION_MESSAGE;
				
				JOptionPane.showMessageDialog(ShutdownWindow.this, "Tested on Windows 7 Ultimate 64-Bit\n" + "with Java Runtime Environment:\n    v"
						+ testVersion + "\n\nYou're using:\n    " + System.getProperty("sun.arch.data.model") + "-Bit JRE\n    v" + activeVersion,
						"Troubleshoot", status);
			}
		});
		menu.add(menuItem);
		menu.addSeparator();
		
		menuItem = new JMenuItem("About");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(ShutdownWindow.this, "Shutdown v1.0\n" + "    by Velibor Baèujkov\n" + "    2014.", "About",
						JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getIconImage()));
			}
		});
		menu.add(menuItem);
		
		setJMenuBar(menuBar);
		
		/* OTHER */
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(mTimer != null) {
					if(mTrayIcon != null) {
						int choice = JOptionPane.showOptionDialog(ShutdownWindow.this, "Shutdown timer is set. \nAre you sure you want to exit?", "Shutdown",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
						
						switch(choice)
						{
							case JOptionPane.NO_OPTION:
							case JOptionPane.CLOSED_OPTION:
								return;
						}
					}
					else {
						String[] options = new String[] { "Minimize", "Exit", "Cancel" };
						int choice = JOptionPane.showOptionDialog(ShutdownWindow.this, "Exit and cancel shutdown timer?", "Shutdown",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						
						switch(choice)
						{
							case 0:
								minimizeToTray();
							case 2:
							case JOptionPane.CLOSED_OPTION:
								return;
						}
					}
				}
				
				cancelShutdown();
				sServer.close();
				dispose();
				System.exit(0);
			}
		});
		
		try {
			BufferedImage image = ImageIO.read(ShutdownWindow.class.getResource("/icon.png"));
			setIconImage(image);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setMinimumSize(getSize());
	}
	
	public ShutdownWindow(long milis) {
		this();
		shutdownAt(new Date(System.currentTimeMillis() + milis));
	}
	
	private boolean mActivated = true;
	
	private void setActivated(boolean activated) {
		mActivated = activated;
		
		mTimeButton.setEnabled(activated);
		mDateButton.setEnabled(activated);
		
		if(activated) {
			mTimePanel.setEnabled(mTimeButton.isSelected());
			mDatePanel.setEnabled(mDateButton.isSelected());
		}
		else {
			mTimePanel.setEnabled(false);
			mDatePanel.setEnabled(false);
		}
		
		mButton.setText(activated ? "Set" : "Cancel");
		mTimerLabel.setText(activated ? "timer off" : "");
	}
	
	private ShutdownTray mTrayIcon;
	
	private void minimizeToTray() {
		if(SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			
			Dimension dim = tray.getTrayIconSize();
			Image image = Utils.resizeImage(getIconImage(), dim.width, dim.height, true);
			
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
					tray.remove(mTrayIcon);
					mTrayIcon = null;
				}
			};
			
			PopupMenu popup = new PopupMenu();
			MenuItem menu;
			
			menu = new MenuItem("Maximize");
			menu.addActionListener(listener);
			popup.add(menu);
			
			menu = new MenuItem("Exit");
			menu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispatchEvent(new WindowEvent(ShutdownWindow.this, WindowEvent.WINDOW_CLOSING));
				}
			});
			popup.add(menu);
			
			String info = Utils.getTime(mShutdownTime - Calendar.getInstance().getTimeInMillis()).toString();
			mTrayIcon = new ShutdownTray(image, info, popup);
			mTrayIcon.addActionListener(listener);
			try {
				tray.add(mTrayIcon);
				setVisible(false);
			} catch(AWTException e) {
				e.printStackTrace();
			}
		}
		else {
			setState(Frame.ICONIFIED);
		}
	}
	
	private static String sShutdownCommand;
	
	private static int sThreadId = -1;
	private Thread mTimer;
	private boolean mRunning = false;
	
	private long mShutdownTime;
	
	private void shutdownAt(Date time) {
		cancelShutdown();
		setActivated(false);
		
		mShutdownTime = time.getTime();
		mDialogShown = mShutdownTime - Calendar.getInstance().getTimeInMillis() < 10000;
		
		++sThreadId;
		mTimer = new Thread(this);
		
		mRunning = true;
		mTimer.start();
	}
	
	private void cancelShutdown() {
		if(mTimer != null) {
			mRunning = false;
			mTimer = null;
			setActivated(true);
		}
	}
	
	private boolean mDialogShown = false;
	private JLabel mWarningLabel = null;
	
	private void showWarningDialog() {
		mDialogShown = true;
		new Thread("Warning") {
			@Override
			public void run() {
				mWarningLabel = new JLabel("Shutdown in less than 2 minutes!");
				String[] options = new String[] { "Shutdown now", "Cancel timer", "Dismiss message" };
				int choice = JOptionPane.showOptionDialog(ShutdownWindow.this, mWarningLabel, "Shutdown", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[2]);
				switch(choice)
				{
					case 0:
						mShutdownTime = 0;
					break;
					
					case 1:
						cancelShutdown();
					break;
				}
				mWarningLabel = null;
			}
		}.start();
	}
	
	@Override
	public void run() {
		final int id = sThreadId;
		
		while(mRunning && sThreadId == id) {
			long time = mShutdownTime - Calendar.getInstance().getTimeInMillis();
			
			if(time <= 0) {
				try {
					Runtime.getRuntime().exec(sShutdownCommand);
					System.exit(0);
				} catch(IOException e) {
					JOptionPane.showMessageDialog(ShutdownWindow.this, "Cannot initiate shutdown.", "Error!", JOptionPane.ERROR_MESSAGE);
				}
				mRunning = false;
				return;
			}
			
			long sleepTime;
			if(time > 360000) sleepTime = 60000;
			else if(time > 300000) sleepTime = time - 300000;
			else {
				sleepTime = 1000;
				if(!mDialogShown && time < 120000) showWarningDialog();
			}
			
			String info = Utils.getTime(time).toString();
			mTimerLabel.setText(info);
			if(mWarningLabel != null) mWarningLabel.setText("Shutdown in: " + info);
			if(mTrayIcon != null) mTrayIcon.setToolTip("Shutdown in: \n" + info);
			
			try {
				Thread.sleep(sleepTime);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static ShutdownServer sServer = null;
	
	public static void main(String[] args) {
		String os = System.getProperty("os.name");
		System.out.println("OS: " + os);
		
		if(os.startsWith("Linux") || os.startsWith("Mac OS X")) sShutdownCommand = "shutdown -h now";
		else if(os.startsWith("Windows")) sShutdownCommand = "shutdown.exe -s -t 0";
		else {
			JOptionPane.showMessageDialog(null, "Operating system not supported.", "Shutdown", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		try {
			Socket socket = new Socket("localhost", ShutdownServer.PORT);
			try {
				socket.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			JOptionPane.showMessageDialog(null, "Already running!", "Shutdown", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		} catch(IOException e) {
			sServer = new ShutdownServer();
			sServer.start();
		}
		
		int sec = -1;
		if(args.length > 0) {
			try {
				sec = Integer.parseInt(args[0]);
			} catch(NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		
		if(sec >= 0) {
			new ShutdownWindow(sec * 1000);
		}
		else {
			new ShutdownWindow();
		}
	}
	
}
