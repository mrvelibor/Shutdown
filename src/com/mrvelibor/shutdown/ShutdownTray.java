package com.mrvelibor.shutdown;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;

public class ShutdownTray extends TrayIcon {
	
	private final MenuItem mTrayInfo;
	
	public ShutdownTray(Image image, String tooltip, PopupMenu popup) {
		super(image);
		
		mTrayInfo = new MenuItem();
		mTrayInfo.setEnabled(false);
		
		popup.insert(mTrayInfo, 0);
		popup.insertSeparator(1);
		
		setPopupMenu(popup);
		setToolTip("Shutdown in: \n" + tooltip);
	}
	
	@Override
	public void setToolTip(String tooltip) {		
		mTrayInfo.setLabel(tooltip);
		super.setToolTip(tooltip);
	}
	
}
